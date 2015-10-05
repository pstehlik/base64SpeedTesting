@Grab(group='com.brsanthu', module='migbase64', version='2.2')
@Grab(group='org.bouncycastle', module='bcprov-jdk15on', version='1.52')
@Grab(group='commons-codec', module='commons-codec', version='1.10')

import com.migcomponents.migbase64.Base64
import org.bouncycastle.util.encoders.Base64 as BC_Base64
import org.apache.commons.codec.binary.Base64 as Apache_Base64
import javax.xml.bind.DatatypeConverter



void encodeDecodeLoop(File someFile, iterations, libname, encode, decode){
  println "---"
  println "${libname}: iterations [${iterations}]"
  byte[] someBytes = someFile.readBytes()
  def avg = totalDur = count = totalChars = 0

  iterations.times{ it ->
    //don't want the exact same string in each loop
    def start = System.currentTimeMillis()
    String textB64 = encode(someBytes)
    decode(textB64)
        
    def end = System.currentTimeMillis()
    def dur = end - start
//    println "[${count}] took [${dur}]ms"
    totalDur += dur
    count++
  }
  avg = totalDur / count
  println "${libname}: Time/iterations total [${totalDur}]ms, average [${avg}]ms"
}




def generateMD5( File file ) {
  def digest = java.security.MessageDigest.getInstance("MD5")
  file.eachByte( 4096 ) { buffer, length ->
    digest.update( buffer, 0, length )
  }
  new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
}

//Making sure the encoders and decoders to the same things
void checkEncoders(List encdrs, List dcdrs, File inputFile){
  String originalMD5 = generateMD5(inputFile)
  encdrs.eachWithIndex{ enc, Integer ix ->
    String str = enc(inputFile.getBytes())
    dcdrs.eachWithIndex{dec, ix2 ->
      File tmp = File.createTempFile("temp",".tmp")
      tmp.withOutputStream {
        it.write(dec(str))
      }
      def newMD5 = generateMD5(tmp)
//      println "Checking encoder [${ix}} with decoder [${ix2}]"
//      println "MD5 [${originalMD5}] to MD5 [${newMD5}]"
      assert originalMD5 == newMD5
    }
  }
}


def encodeGroovy = { byte[] someBytes ->
  someBytes.encodeBase64().toString()
}

def decodeGroovy = { String text ->
  text.decodeBase64()
}

def encodeMig = { byte[] someBytes ->
  Base64.encodeToString(someBytes, false)
}

def decodeMig = { String text ->
  Base64.decode(text.getBytes("UTF-8"))
}

def encodeBouncyCastle = { byte[] someBytes ->
  new String(BC_Base64.encode(someBytes))
}

def decodeBouncyCastle = { String text ->
  BC_Base64.decode(text)
}

def encodeJava = { byte[] someBytes ->
  DatatypeConverter.printBase64Binary(someBytes)
}

def decodeJava = { String text ->
  DatatypeConverter.parseBase64Binary(text)
}

def encodeApacheCommons = { byte[] someBytes ->
  new String(Apache_Base64.encodeBase64(someBytes))
}

def decodeApacheCommons = { String text ->
  Apache_Base64.decodeBase64(text)
}

def encoders = [
    encodeGroovy, encodeMig, encodeBouncyCastle, encodeJava, encodeApacheCommons
]

def decoders = [
    decodeGroovy, decodeMig, decodeBouncyCastle, decodeJava, decodeApacheCommons
]

//-----------------------------------------------------------------------------

if(args.size() < 1){
  println "call groovy testBase64.groovy <loop count for encode/decode loop>"
  println "e.g. `groovy testBase64.groovy 100`"
  return
}
def encodeDecodeLoopCount = Integer.parseInt(args[0])

def files = [
    new File("small_file"),
    new File("med_file"),
    new File("large_file")
]
files.each {File fl ->
  println "Using file [${fl.absolutePath}], length [${fl.length()}]"
  println "checking encoders and decoders"
  checkEncoders(encoders, decoders, fl)
}

int iters = 10
println ""
println "--- warmup ---"
files.each { File fl ->
  println "Working with file [${fl.absolutePath}]"
  encodeDecodeLoop(fl, iters, 'groovy', encodeGroovy, decodeGroovy)
  encodeDecodeLoop(fl, iters, 'migBase64', encodeMig, decodeMig)
  encodeDecodeLoop(fl, iters, 'bouncyCastle', encodeBouncyCastle, decodeBouncyCastle)
  encodeDecodeLoop(fl, iters, 'java DatatypeConverter', encodeJava, decodeJava)
  encodeDecodeLoop(fl, iters, 'Apache Commons', encodeApacheCommons, decodeApacheCommons)
}

iters = encodeDecodeLoopCount
println ""
println "--- real execution ---"
files.each { File fl ->
  println "Working with file [${fl.absolutePath}]"
  encodeDecodeLoop(fl, iters, 'groovy', encodeGroovy, decodeGroovy)
  encodeDecodeLoop(fl, iters, 'migBase64', encodeMig, decodeMig)
  encodeDecodeLoop(fl, iters, 'bouncyCastle', encodeBouncyCastle, decodeBouncyCastle)
  encodeDecodeLoop(fl, iters, 'java DatatypeConverter', encodeJava, decodeJava)
  encodeDecodeLoop(fl, iters, 'Apache Commons', encodeApacheCommons, decodeApacheCommons)
}
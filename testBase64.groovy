@Grab(group='com.brsanthu', module='migbase64', version='2.2')
@Grab(group='org.bouncycastle', module='bcprov-jdk15on', version='1.52')
@Grab(group='commons-codec', module='commons-codec', version='1.10')



import com.migcomponents.migbase64.Base64
import org.bouncycastle.util.encoders.Base64 as BC_Base64
import org.apache.commons.codec.binary.Base64 as Apache_Base64
import javax.xml.bind.DatatypeConverter

if(args.size() < 2){
	println "call groovy testBase64.groovy <loop count to create text> <loop count for encode/decode loop>"
	println "for example `groovy testBase64.groovy 5000 500`"
	return
}
def createTextLoop = Integer.parseInt(args[0])
def encodeDecodeLoopCount = Integer.parseInt(args[1])

String someTxt = ""

println "Generating text"
//fill in "long" text - not very efficient but only needed once
createTextLoop.times{
	someTxt += "I am a long text file with lots of text in each line that will generate a lot of text.${System.lineSeparator()}"
}


println "Text size [${someTxt.length()}]"


void encodeDecodeLoop(someText, iterations, libname, encode, decode){
	header(libname, iterations)
	def avg = totalDur = count = 0
	String text 
	iterations.times{ it ->
		//don't want the exact same string in each loop
		text = new String(someText + it)

		def start = System.currentTimeMillis()
		String textB64 = encode(text)
	
		String lT = decode(textB64)
				
		def end = System.currentTimeMillis()
		def dur = end - start
/*		println "[${count}] took [${dur}]ms"*/
		totalDur += dur
		count++
		
		assert lT == text
	}
	avg = totalDur / count
	footer(libname, avg, totalDur)	
}

void header(which, iter){
	println "---"
	println "$which: iterations [${iter}]"
}

void footer(which, avg, totalDur){
	println "$which: total [${totalDur}]ms, average [${avg}]ms"
}


def encodeGroovy = { String text ->
	text.bytes.encodeBase64().toString()
}

def decodeGroovy = { String text ->
	new String(text.decodeBase64())
}

def encodeMig = { String text ->
	Base64.encodeToString(text.getBytes("UTF-8"), false)
}

def decodeMig = { String text ->
	new String(Base64.decode(text.getBytes("UTF-8")))
}

def encodeBouncyCastle = { String text ->
	new String(BC_Base64.encode(text.getBytes("UTF-8")))
}

def decodeBouncyCastle = { String text ->
	new String(BC_Base64.decode(text))
}

def encodeJava = { String text ->
	DatatypeConverter.printBase64Binary(text.getBytes("UTF-8"))
}

def decodeJava = { String text ->
	new String(DatatypeConverter.parseBase64Binary(text))
}

def encodeApacheCommons = { String text ->
	new String(Apache_Base64.encodeBase64(text.getBytes("UTF-8")))
}

def decodeApacheCommons = { String text ->
	new String(Apache_Base64.decodeBase64(text))
}


int iters = 50
println ""
println "--- warmup ---"
encodeDecodeLoop(someTxt, iters, 'groovy', encodeGroovy, decodeGroovy)
encodeDecodeLoop(someTxt, iters, 'migBase64', encodeMig, decodeMig)
encodeDecodeLoop(someTxt, iters, 'bouncyCastle', encodeBouncyCastle, decodeBouncyCastle)
encodeDecodeLoop(someTxt, iters, 'java DatatypeConverter', encodeJava, decodeJava)
encodeDecodeLoop(someTxt, iters, 'Apache Commons', encodeApacheCommons, decodeApacheCommons)

iters = encodeDecodeLoopCount
println ""
println "--- for real ---"
encodeDecodeLoop(someTxt, iters, 'groovy', encodeGroovy, decodeGroovy)
encodeDecodeLoop(someTxt, iters, 'migBase64', encodeMig, decodeMig)
encodeDecodeLoop(someTxt, iters, 'bouncyCastle', encodeBouncyCastle, decodeBouncyCastle)
encodeDecodeLoop(someTxt, iters, 'java DatatypeConverter', encodeJava, decodeJava)
encodeDecodeLoop(someTxt, iters, 'Apache Commons', encodeApacheCommons, decodeApacheCommons)
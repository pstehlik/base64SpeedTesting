@Grab(group='com.brsanthu', module='migbase64', version='2.2')
@Grab(group='org.bouncycastle', module='bcprov-jdk15on', version='1.52')
@Grab(group='commons-codec', module='commons-codec', version='1.10')



import com.migcomponents.migbase64.Base64
import org.bouncycastle.util.encoders.Base64 as BC_Base64
import org.apache.commons.codec.binary.Base64 as Apache_Base64
import javax.xml.bind.DatatypeConverter


String shortTxt = "some short text"
String longTxt = ""

println "Generating long text"
//fill in "long" text - not very efficient but only needed once
5000.times{
	longTxt += "I am a long text file with lots of text in each line that will generate a lot of text.${System.lineSeparator()}"
}


println "shortTxt size [${shortTxt.length()}]"
println "longTxt size [${longTxt.length()}]"


void encodeDecodeLoop(shortText, longText, iterations, libname, encode, decode){
	header(libname, iterations)
	def avg = 0
	def totalDur = 0
	def single
	def count = 0
	iterations.times{ it ->
		shortText = shortText + it
		longText = longText + it
		
		def start = System.currentTimeMillis()
		String shortTextB64 = encode(shortText)
		String longTextB64 = encode(longText)
	
		String sT = decode(shortTextB64)
		String lT = decode(longTextB64)
				
		def end = System.currentTimeMillis()
		def dur = end - start
/*		println "[${count}] took [${dur}]ms"*/
		totalDur += dur
		count++
		
		assert sT == shortText
		assert lT == longText
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
encodeDecodeLoop(shortTxt, longTxt, iters, 'groovy', encodeGroovy, decodeGroovy)
encodeDecodeLoop(shortTxt, longTxt, iters, 'migBase64', encodeMig, decodeMig)
encodeDecodeLoop(shortTxt, longTxt, iters, 'bouncyCastle', encodeBouncyCastle, decodeBouncyCastle)
encodeDecodeLoop(shortTxt, longTxt, iters, 'java DatatypeConverter', encodeJava, decodeJava)
encodeDecodeLoop(shortTxt, longTxt, iters, 'Apache Commons', encodeApacheCommons, decodeApacheCommons)

iters = 300
println ""
println "--- for real ---"
encodeDecodeLoop(shortTxt, longTxt, iters, 'groovy', encodeGroovy, decodeGroovy)
encodeDecodeLoop(shortTxt, longTxt, iters, 'migBase64', encodeMig, decodeMig)
encodeDecodeLoop(shortTxt, longTxt, iters, 'bouncyCastle', encodeBouncyCastle, decodeBouncyCastle)
encodeDecodeLoop(shortTxt, longTxt, iters, 'java DatatypeConverter', encodeJava, decodeJava)
encodeDecodeLoop(shortTxt, longTxt, iters, 'Apache Commons', encodeApacheCommons, decodeApacheCommons)
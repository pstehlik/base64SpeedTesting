@Grab(group='com.brsanthu', module='migbase64', version='2.2')

import com.migcomponents.migbase64.Base64

String shortTxt = "some short text"
String longTxt = ""

println "Generating long text"
//fill in "long" text - not very efficient but only needed once
10000.times{
	longTxt += "I am a long text file with lots of text in each line that will generate a lot of text.${System.lineSeparator()}"
}


println "shortTxt size [${shortTxt.length()}]"
println "longTxt size [${longTxt.length()}]"


void encodeDecodeLoop(shortText, longText, iterations, libname, encode, decode){
	header(libname, iterations)
	def avg = 0
	def single
	def count = 0
	iterations.times{ 
		def start = System.currentTimeMillis()
		String shortTextB64 = encode(shortText)
		String longTextB64 = encode(longText)
	
		String sT = decode(shortTextB64)
		String lT = decode(longTextB64)

		assert sT.length() == shortText.length()
		assert lT.length() == longText.length()		
		
		def end = System.currentTimeMillis()
		def dur = end - start
/*		println "[${count}] took [${dur}]ms"*/
		avg += dur
		count++
	}
	avg = avg / count
	footer(libname, avg)	
}

void header(which, iter){
	println "$which: iterations [${iter}]"
}

void footer(which, avg){
	println "$which: average [${avg}]ms"
}


def encodeGroovy = { String text ->
	text.bytes.encodeBase64().toString()
}

def decodeGroovy = { String text ->
	new String(text.decodeBase64())
}

def encodeMig = { String text ->
	Base64.encodeToString(text.bytes, false)
}

def decodeMig = { String text ->
	new String(Base64.decode(text.bytes))
}

int iters = 50
println "--- warmup ---"
encodeDecodeLoop(shortTxt, longTxt, iters, 'groovy', encodeGroovy, decodeGroovy)
encodeDecodeLoop(shortTxt, longTxt, iters, 'migBase64', encodeMig, decodeMig)

iters = 300
println "--- for real ---"
encodeDecodeLoop(shortTxt, longTxt, iters, 'groovy', encodeGroovy, decodeGroovy)
encodeDecodeLoop(shortTxt, longTxt, iters, 'migBase64', encodeMig, decodeMig)

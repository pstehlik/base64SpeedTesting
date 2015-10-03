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


void doWithGroovy(shortText, longText, iterations){

	header("groovy", iterations)
	def avg = 0
	def single
	def count = 0
	iterations.times{ 
		def start = System.currentTimeMillis()
		String shortTextB64 = shortText.bytes.encodeBase64().toString()
		String longTextB64 = longText.bytes.encodeBase64().toString()
	
		String sT = new String(shortTextB64.decodeBase64())
		String lT = new String(longTextB64.decodeBase64())

		assert sT.length() == shortText.length()
		assert lT.length() == longText.length()		
		
		def end = System.currentTimeMillis()
		def dur = end - start
/*		println "[${count}] took [${dur}]ms"*/
		avg += dur
		count++
	}
	avg = avg / count
	footer("groovy", avg)
}


void doWithMig(shortText, longText, iterations){

	header("migbase64", iterations)
	def avg = 0
	def single
	def count = 0
	iterations.times{ 
		def start = System.currentTimeMillis()
		String shortTextB64 = Base64.encodeToString(shortText.bytes, false)
		String longTextB64 = Base64.encodeToString(longText.bytes, false)
	
		String sT = new String(Base64.decode(shortTextB64.bytes))
		String lT = new String(Base64.decode(longTextB64.bytes))

		assert sT.length() == shortText.length()
		assert lT.length() == longText.length()		
		
		def end = System.currentTimeMillis()
		def dur = end - start
/*		println "[${count}] took [${dur}]ms"*/
		avg += dur
		count++
	}
	avg = avg / count
	footer("migbase64", avg)
}


void header(which, iter){
	println "$which: iterations [${iter}]"
}

void footer(which, avg){
	println "$which: average [${avg}]ms"
}

println "--- warmup ---"
doWithGroovy(shortTxt, longTxt, 50)
doWithMig(shortTxt, longTxt, 50)

println "--- for real ---"
doWithGroovy(shortTxt, longTxt, 500)
doWithMig(shortTxt, longTxt, 500)

println "--- for real again ---"
doWithMig(shortTxt, longTxt, 500)
doWithGroovy(shortTxt, longTxt, 500)
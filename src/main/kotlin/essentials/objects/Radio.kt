package essentials.objects

interface Radio {
    fun start()
    fun stop()
}

fun main() {
//    val radio: Radio = Radio()
//    val sonyRadio: Radio = Radio.sony()
//    val panasonicRadio: Radio = Radio.panasonic()
//    val sonyRadio2: Radio = RadioMaker.sony()
//    val panasonicRadio2: Radio = RadioMaker.panasonic()
//    val factory = RadioFactory()
//    val sonyRadio3: Radio = factory.makeSonyRadio()
//    val panasonicRadio3: Radio = factory.makePanasonicRadio()
}

private class SonyRadio() : Radio {
    override fun start() {
        print("Start Sony radio")
    }

    override fun stop() {
        print("Stop Sony radio")
    }
}

private class PanasonicRadio : Radio {
    override fun start() {
        print("Start Sony radio")
    }

    override fun stop() {
        print("Stop Sony radio")
    }
}

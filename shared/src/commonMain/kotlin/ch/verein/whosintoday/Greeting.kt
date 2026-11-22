package ch.verein.whosintoday

class Greeting {
    private val platform = getPlatform()

    fun greet(): String = sayHello(platform.name)
}

package utils

/* External imports */
import scala.io.StdIn


object Console {
    private var PROMPT: String = Constants.DefaultPrompt

    def setDefaultPrompt(): Unit = {
        this.PROMPT = Constants.DefaultPrompt
    }

    def setCommandPrompt(prompt: String): Unit = {
        this.PROMPT = prompt
    }

    def waiting(): Unit = {
        StdIn.readLine(this.PROMPT)
        
    }

    def readingString(): String = {
        return StdIn.readLine(this.PROMPT)
    }

    def readingInt(): Int = {
        print(PROMPT)
        return StdIn.readInt()
    }

    def exiting(): Unit = {
        StdIn.readLine("Press any key to exit...")
    }
}

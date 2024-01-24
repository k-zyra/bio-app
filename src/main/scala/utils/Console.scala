package utils

/* External imports */
import scala.io.StdIn


object Console {
    private var PROMPT: String = Constants.DEFAULT_PROMPT

    def setDefaultPrompt(): Unit = {
        this.PROMPT = Constants.DEFAULT_PROMPT
    }

    def setCommandPrompt(prompt: String): Unit = {
        this.PROMPT = prompt
    }

    def running(): Unit = {
        StdIn.readLine(this.PROMPT)
        
    }

    def getInput(): Unit = {

    }

    def exiting(): Unit = {
        StdIn.readLine("Press any key to exit...")
    }
}

package bio.align.multiple

/* Internal imports */
import misc.Logger



object Config {
    private val logger = new Logger("MSA_Configuration")

    final val EARLY_STAGE_PROGRESS: Double = 0.1
    final val ENDING_STAGE_PROGRESS: Double = 0.9

    private var configured: Boolean = false
    var preprocess: Boolean = true

    var epoch: Int = 0
    var epochsInPlateau: Int = 0
    var currentBest: Int = Int.MinValue

    var maxEpoch: Int = 50
    var keepGoing: Boolean = false

    var maxOffset: Int = 0
    var maxExpectedOffspring: Int = 0

    var generationSize: Int = 0
    var replacementSize: Int = 0
    var reproductionSize: Int = 0
    var mutationSize: Int = 0

    var initialAverageLength: Int = 0


    def getEvolutionProgress(): Double = {
        this.epoch.toDouble/this.maxEpoch.toDouble
    }


    def isEarlyStage(): Boolean = {
        this.getEvolutionProgress() < this.EARLY_STAGE_PROGRESS
    }


    def isEndingStage(): Boolean = {
        this.getEvolutionProgress() > this.ENDING_STAGE_PROGRESS
    }


    /* Set algorithm's settings
    */
    def set(_maxNumberOfEpochs: Int = 100,
          _generationSize: Int = 100,
          _maxOffset: Int = 5,
          _replacementFactor: Double = 0.5,
          _reproductionFactor: Double = 0.5,
          _maxExpectedOffspring: Int = 2,
          _preprocess: Boolean = true): Unit = {
        this.generationSize = _generationSize
        this.maxOffset = _maxOffset
        this.replacementSize = (_replacementFactor * this.generationSize).toInt
        this.reproductionSize = (_reproductionFactor * (this.generationSize - this.replacementSize)).toInt
        this.mutationSize = this.replacementSize - this.reproductionSize
        this.maxExpectedOffspring = _maxExpectedOffspring
        this.maxEpoch = _maxNumberOfEpochs

        this.preprocess = _preprocess
        this.configured = true
    }


    /* Reset configuration for the beginning of the algorithm
    */
    def reset(): Unit = {
        this.epoch = 0
        this.keepGoing = true
    }


    /* Check whether the algorithm's settings are prepared
    */
    def isSet(): Unit = {
        if (this.configured) {
            logger.logInfo("Continuing with given configuration.")
        } else {
            this.set()
            logger.logWarn("Configured using default values.")
        }
    }
}

val scala2Version = "2.12.13"

enablePlugins(JavaAppPackaging)


lazy val root = project
    .in(file("."))
    .settings(
        name := "bio-app",
        version := "1.0.0",
        scalaVersion := scala2Version,

        // Library dependecies
        libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
        libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.1.1" % Test,

        libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.0" % "provided",
        libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.1.2" % "provided",
        libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.2.0" % "provided",

        libraryDependencies += "org.scalanlp" %% "breeze" % "2.1.0",
        libraryDependencies += "org.scalanlp" %% "breeze-viz" % "0.13.2",

        libraryDependencies += "com.github.scopt" %% "scopt" % "4.1.0",
        libraryDependencies += "com.github.mrpowers" %% "spark-daria" % "1.2.3",
        libraryDependencies += "com.github.vickumar1981" %% "stringdistance" % "1.2.7",
        libraryDependencies += "com.github.pathikrit" %% "better-files-akka" % "3.9.2",
        libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.9.3",
        libraryDependencies += "com.nvidia" %% "rapids-4-spark" % "23.12.0" % "provided",

        libraryDependencies += "pl.project13.scala" % "sbt-jmh" % "2.12.1" % "0.4.0",
        libraryDependencies += "org.openjdk.jmh" % "jmh-core" % "1.32" % "test",
        libraryDependencies += "org.openjdk.jmh" % "jmh-generator-annprocess" % "1.32" % "test",

        // Bio
        libraryDependencies += "org.biojava" % "biojava-core" % "5.3.0",

        JmhPlugin.projectSettings ++ Seq(
                libraryDependencies += "org.apache.spark" %% "spark-core" % "3.5.0" % "provided",
                libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.1.2" % "provided",
                libraryDependencies += "org.apache.spark" %% "spark-mllib" % "3.2.0" % "provided",
        )

    )

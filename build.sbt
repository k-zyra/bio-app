val scala2Version = "2.12.13"

lazy val root = project
  .in(file("."))
  .settings(
    name := "bio-app",
    version := "1.0.0",

    scalaVersion := scala2Version,
    
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
  )

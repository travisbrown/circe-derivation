val catsVersion = "0.5.1"
val benchSize = 1
val benchReps = 1

val buildSettings = Seq(
  organization := "io.circe",
  scalaVersion := "2.11.8",
  scalacOptions ++= Seq(
    "-feature",
    "-language:higherKinds",
    "-Ywarn-unused-import",
    "-Xfuture"
  ),
  scalacOptions in (Compile, console) ~= {
    _.filterNot(Set("-Ywarn-unused-import"))
  },
  autoAPIMappings := true,
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-core" % catsVersion,
    "io.circe" %% "circe-jawn" % catsVersion,
    "io.circe" %% "circe-literal" % catsVersion,
    "com.chuusai" %% "shapeless" % "2.3.2",
    "org.typelevel" %% "macro-compat" % "1.1.1",
    "org.typelevel" %% "export-hook" % "1.1.0",
    compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )
)

val core = project.settings(buildSettings)
val definitions = project.settings(buildSettings).settings(
  sourceGenerators in Compile <+= (sourceManaged in Compile).map(Boilerplate.ccs(benchSize)),
  sourceGenerators in Compile <+= (sourceManaged in Compile).map(Boilerplate.adts(benchSize)),
  sourceGenerators in Compile <+= (sourceManaged in Compile).map(Boilerplate.deeps(benchSize))
)

val bench = project.settings(buildSettings).settings(
  libraryDependencies ++= Seq(
    "io.circe" %% "circe-generic" % catsVersion,
    "org.scalacheck" %% "scalacheck" % "1.13.2"
  ),
  sourceGenerators in Compile <+= (sourceManaged in Compile).map(
    Boilerplate.benches(benchSize, benchReps)
  )
).dependsOn(core, definitions)

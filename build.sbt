
inThisBuild(
  List(
    scalaVersion := "2.12.2",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-language:existentials",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-unchecked",
      "-Xlint",
      "-Yno-adapted-args",
      "-Ywarn-dead-code",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Xfuture"
    )
  ))

lazy val root = project
  .in(file("."))
  .settings(
    name := "simple-backpropagation",
    libraryDependencies ++= Seq(
      "co.fs2"                     %% "fs2-core"                       % "0.9.6",
      "co.fs2"                     %% "fs2-cats"                       % "0.3.0"
    )
  )
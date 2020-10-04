import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }

name := "Hyper Loop Runner"

version := "1.0"

lazy val ld47 =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .settings(Seq(
    scalaVersion := "2.13.3",
    libraryDependencies ++= List(
      "eu.joaocosta"                 %%% "minart-core"   % "0.1.6",
      "eu.joaocosta"                 %%% "minart-pure"   % "0.1.6"
    )
  ))
  .jsSettings(Seq(
    scalaJSUseMainModuleInitializer := true
  ))
  .nativeSettings(Seq(
    scalaVersion := "2.11.12",
    nativeLinkStubs := true,
    nativeMode := "release"
  ))
  .settings(name := "ld47")

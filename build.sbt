import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }

name := "Hyper Loop Runner"

version := "1.4"

lazy val ld47 =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .settings(Seq(
    scalaVersion := "3.2.2",
    libraryDependencies ++= List(
      "eu.joaocosta" %%% "minart" % "0.5.0-SNAPSHOT",
    )
  ))
  .jsSettings(Seq(
    scalaJSUseMainModuleInitializer := true
  ))
  .nativeSettings(Seq(
    nativeLinkStubs := true,
    nativeMode := "release-full",
    nativeLTO := "thin",
    nativeGC := "immix"
  ))
  .settings(name := "ld47")

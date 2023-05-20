import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }

name := "Hyper Loop Runner"

version := "1.5"

lazy val ld47 =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .settings(Seq(
    scalaVersion := "3.2.2",
    libraryDependencies ++= List(
      "eu.joaocosta" %%% "minart" % "0.5.2",
    )
  ))
  .jsSettings(Seq(
    scalaJSUseMainModuleInitializer := true
  ))
  .nativeSettings(Seq(
    nativeLinkStubs := true,
    nativeMode := "release-full",
    nativeLTO := "thin",
    nativeGC := "commix",
    nativeConfig ~= {
      _.withEmbedResources(true)
    }
  ))
  .settings(name := "ld47")

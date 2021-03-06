import sbtcrossproject.CrossPlugin.autoImport.{ crossProject, CrossType }

name := "Hyper Loop Runner"

version := "1.1"

lazy val ld47 =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .in(file("."))
  .settings(Seq(
    scalaVersion := "2.13.4",
    libraryDependencies ++= List(
      "eu.joaocosta"                 %%% "minart-core"   % "0.2.0",
      "eu.joaocosta"                 %%% "minart-pure"   % "0.2.0",
    ),
    scalacOptions in Test ++= Seq("-unchecked", "-deprecation")
  ))
  .jsSettings(Seq(
    scalaJSUseMainModuleInitializer := true
  ))
  .nativeSettings(Seq(
    nativeLinkStubs := true,
    nativeMode := "release-full",
    nativeLTO := "thin",
    nativeGC := "commix"
  ))
  .settings(name := "ld47")

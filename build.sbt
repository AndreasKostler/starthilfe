uniform.project("starthilfe", "au.com.cba.omnia.starthilfe")

val omnitoolVersion    = "1.12.0-20151021050758-700b9d0"
val thermometerVersion = "1.3.0-20151122230202-55282c8"
val ebenezerVersion    = "0.21.2-20160104234501-8e2043a"
val rengineVersion     = "2.1.0"

uniformDependencySettings
uniformThriftSettings
strictDependencySettings

libraryDependencies :=
  depend.testing() ++ 
  depend.omnia("ebenezer", ebenezerVersion, "test") ++
  depend.omnia("omnitool-core", omnitoolVersion) ++
  depend.omnia("thermometer-hive", thermometerVersion, "test") ++
  Seq(
    "org.specs2"               %% "specs2-core"   % depend.versions.specs,
    "au.com.cba.omnia"         %% "omnitool-core" % omnitoolVersion % "test" classifier "tests",
    "org.rosuda.REngine"       % "REngine"        % rengineVersion
  )


// R environment variables
val rHome: String = sys.env.get("R_HOME").getOrElse("/Library/Frameworks/R.framework/Resources")
val jriLDPath: String = s"$rHome/lib:$rHome/bin:"
val ldLibraryPath: String = sys.env.get("LD_LIBRARY_PATH") map (_ ++ s":$jriLDPath") getOrElse(jriLDPath)
val rPath = s"$rHome/library/rJava/jri"
val path: String = sys.env.get("PATH") map (_ ++ s":$rPath") getOrElse(rPath)

unmanagedJars in Test <++= baseDirectory map { base =>
  val baseDirectories = base +++ file(s"$rHome/library/rJava/jri")
  val customJars = (baseDirectories ** "*.jar")
  customJars.classpath
}

envVars in Test += "R_HOME" -> rHome
envVars in Test += "LD_LIBRARY_PATH" -> ldLibraryPath
envVars in Test += "PATH" -> path

val libPath = s"${rHome}/library/rJava/jri:" + System.getProperty("Djava.library.path")

javaOptions in Test += s"-Djava.library.path=$libPath"

fork in Test := true

updateOptions                     := updateOptions.value.withCachedResolution(true)
publishArtifact           in Test := true
parallelExecution         in Test := false

uniform.docSettings("https://github.com/CommBank/starthilfe")
uniform.ghsettings


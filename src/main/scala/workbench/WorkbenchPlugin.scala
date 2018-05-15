package com.lihaoyi.workbench
import java.io.File

import sbt._
import sbt.Keys._
import autowire._
import org.scalajs.sbtplugin.ScalaJSPlugin

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global

object WorkbenchPlugin extends AutoPlugin {

  override def requires = WorkbenchBasePlugin

  private def globFiles(dir: String, extension: String): List[File] = {
    val results = new ListBuffer[File]()
    for(file <- new File(dir).listFiles()){
      if(file.isFile && file.getName.endsWith(extension)){
        results.append(file)
      } else if(file.isDirectory){
        results.append(globFiles(file.getAbsolutePath, extension):_*)
      }
    }
    results.toList
  }

  object autoImport {
    val refreshBrowsers = taskKey[Unit]("Sends a message to all connected web pages asking them to refresh the page")
  }
  import autoImport._
  import WorkbenchBasePlugin.server
  import ScalaJSPlugin.AutoImport._

  val modificationTimes = mutable.HashMap[String, Long]()

  val workbenchSettings = Seq(
    refreshBrowsers := {
      streams.value.log.info("workbench: Reloading Pages...")
      val scalaFiles = (
        globFiles((baseDirectory.value / "src").getAbsolutePath, ".scala")
        ++ globFiles((baseDirectory.value / "target/scala-2.12/src_managed").getAbsolutePath, ".scala")
      )
      var changed = false
      for(file <- scalaFiles){
        modificationTimes.get(file.getAbsolutePath) match {
          case Some(lastModified) => {
            if(lastModified < file.lastModified()){
              modificationTimes.update(file.getAbsolutePath, file.lastModified())
              changed = true
//              println("Changed", file.getAbsolutePath)
            }
          }
          case None => {
            modificationTimes.update(file.getAbsolutePath, file.lastModified())
            changed = true
//            println("Changed", file.getAbsolutePath)
          }
        }
      }
      if(changed){
        println("IT CHANGED")
        server.value.Wire[Api].reload().call()
      }
    },
    
    refreshBrowsers := refreshBrowsers.triggeredBy(fastOptJS in Compile).value
  )

  override def projectSettings = workbenchSettings

}

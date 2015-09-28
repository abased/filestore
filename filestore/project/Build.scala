import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.linux.{LinuxPackageMapping, LinuxSymlink}
import com.typesafe.sbt.packager.rpm.RpmDependencies
import NativePackagerKeys._
import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName = "filestore-webapp"
  val appVersion = "3.1.0-SNAPSHOT" // sosa

  val appDependencies = Seq()

  // play2-maven-plugin 1.2.2 can't handle new dist structure of Play 2.2.5
  // workaround by @maccamlc
  // https://github.com/nanoko-project/maven-play2-plugin/issues/15#issuecomment-24977753
  val distFolder = new File("target/dist")
  distFolder.mkdirs()

  val main = play.Project(appName, appVersion, appDependencies).settings(
    target in com.typesafe.sbt.SbtNativePackager.Universal := distFolder
    , version in Rpm := "3.1.0_SNAPSHOT" // sosa
    , maintainer in Rpm := "sosacorp"
    , description in Rpm := "filestore WebApp"
    , packageSummary in Rpm := "filestore WebApp"
    , packageDescription in Rpm := "filestore WebApp"
    , rpmLicense in Rpm := Some("Copyright 2014 SoSACorp")
    , rpmVendor in Rpm := "sosacorp"
    , rpmGroup in Rpm := Some("sosacorp")
    , rpmBrpJavaRepackJars := false
    , rpmScriptsDirectory in Rpm := new java.io.File("src/rpm/scriptlets")
    , rpmPost in Rpm := Some(
      """
        |WEBAPP_HOME=/usr/share/filestore-webapp
        |APP_HOME=/var/lib/filestore
        |LOG_HOME=/var/log/filestore
        |if [ ! -d "$APP_HOME" ]; then
        |  mkdir $APP_HOME
        |  chown -R sosacorp:wheel $APP_HOME
        |fi
        |if [ ! -d "$LOG_HOME" ]; then
        |  mkdir $LOG_HOME
        |  chown -R sosacorp:wheel $LOG_HOME
        |fi
        |rm -rf $APP_HOME/latest
        |ln -s $WEBAPP_HOME $APP_HOME/latest
        |
        |if [ ! -f /etc/init.d/filestore ]; then
        |cat << 'EOF' > /etc/init.d/filestore
        |#!/bin/bash
        |#
        |#       /etc/rc.d/init.d/filestore
        |#
        |# chkconfig: 345 95 05
        |# description: filestore management script. Note that Play writes the pid file.
        |#
        |
        |source /etc/init.d/functions
        |
        |USER=sosacorp
        |APP_HOME=/var/lib/filestore/latest
        |PIDFILE=/var/run/filestore.pid
        |PLAY_OPTS="-Dpidfile.path=${PIDFILE} -Dlogger.file=/etc/sosacorp/filestore-logger.xml -Dhttp.port=9090"
        |STDOUT_LOG=/var/log/filestore/stdout.log
        |
        |start() {
        |    echo "Starting filestore webapp:"
        |    rm -f ${STDOUT_LOG}
        |    exec ${APP_HOME}/bin/filestore-webapp $PLAY_OPTS > $STDOUT_LOG 2>&1 &
        |    return $?
        |}
        |
        |stop() {
        |    if [ -f $PIDFILE ]; then
        |        echo "Shutting down filestore:"
        |        kill -9 $(cat ${PIDFILE})
        |        rm -f ${PIDFILE}
        |        return $?
        |    else
        |        echo "filestore is not running, no PID file: ${PIDFILE}"
        |        return 0
        |    fi
        |}
        |
        |status() {
        |    if [ -f $PIDFILE ]; then
        |        echo "PID file is present, indicating filestore is running"
        |    else
        |        echo "filestore is stopped"
        |    fi
        |}
        |
        |case "$1" in
        |    start)
        |        start
        |        RETVAL=$?
        |	;;
        |    stop)
        |        stop
        |        RETVAL=$?
        |	;;
        |    restart)
        |        stop
        |        start
        |        RETVAL=$?
        |	;;
        |    status)
        |        status
        |        RETVAL=$?
        |	;;
        |    *)
        |        echo $"Usage: filestore {start|stop|restart|status}"
        |        RETVAL=2
        |	;;
        |esac
        |exit $RETVAL
        |EOF
        |
        |chmod 755 /etc/init.d/filestore
        |
        |chkconfig --add /etc/init.d/filestore
        |fi
        |
        |/etc/init.d/filestore restart
      """.stripMargin)
    , sbt.Keys.javaOptions in Test ++= Seq(
      "-Xmx2g"
      , "-XX:+CMSClassUnloadingEnabled"
      , "-XX:MaxPermSize=1g"
//      , "-Xdebug"
//      , "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9998"
    )
    , testOptions in Test ++= Seq(
      Tests.Argument(TestFrameworks.ScalaTest, "-oFD")
      , Tests.Filter(s => s.endsWith("Suite"))
    )
  ).settings(
    scalacOptions ++= Seq("-deprecation", "-feature", "-unchecked", "-language:postfixOps", "-language:reflectiveCalls")
  )

}

name := """country-informer"""
organization := "com.example"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.8"

libraryDependencies ++= Seq(
  guice,
  "org.scalatestplus.play"        %% "scalatestplus-play"            % "5.0.0" % Test,
  "com.softwaremill.sttp.client3" %% "async-http-client-backend-zio" % "3.0.0",
  "com.softwaremill.sttp.client3" %% "prometheus-backend"            % "3.0.0",
  "com.softwaremill.sttp.client3" %% "slf4j-backend"                 % "3.0.0",
  "com.softwaremill.sttp.client3" %% "circe"                         % "3.0.0",
  "io.circe"                      %% "circe-generic"                 % "0.13.0",
  "com.typesafe.play"             %% "play-json"                     % "2.9.2",
  "eu.timepit"                    %% "refined"                       % "0.10.1",
  "dev.zio"                       %% "zio"                           % "1.0.12",
  "dev.zio"                       %% "zio-metrics"                   % "1.0.1",
  "dev.zio"                       %% "zio-metrics-prometheus"        % "1.0.1",
  "com.softwaremill.macwire"      %% "macros"                        % "2.5.7" % Provided
)

PlayKeys.devSettings := Seq("play.server.http.port" -> "8080")
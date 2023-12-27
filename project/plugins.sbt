// Code Quality and Linting Plugins
addSbtPlugin("org.wartremover"   % "sbt-wartremover"       % "3.1.5")
addSbtPlugin("org.scalastyle"   %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.scalameta"     % "sbt-scalafmt"          % "2.4.6")
addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter"    % "0.8.0")

// Type Checked Documentation Plugin
addSbtPlugin("org.scalameta" % "sbt-mdoc" % "2.3.6")

// Publishing and Release Plugins
addSbtPlugin("io.crashbox"    % "sbt-gpg"            % "0.2.1")
addSbtPlugin("com.github.sbt" % "sbt-release"        % "1.1.0")
addSbtPlugin("ch.epfl.scala"  % "sbt-version-policy" % "2.0.1")

// Other Plugins
addSbtPlugin("org.scoverage"    % "sbt-scoverage" % "1.9.3")
addSbtPlugin("com.timushev.sbt" % "sbt-updates"   % "0.6.3")

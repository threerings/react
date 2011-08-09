React
=====

React is a low-level library that provides [signal/slot] and [functional
reactive programming]-like primitives. It can serve as the basis for a user
interface toolkit, or any other library that has a model on which clients will
listen and to which they will react.

Building
--------

The library is built using [SBT](http://github.com/harrah/xsbt/wiki/Setup).

Invoke `xsbt publish-local` to build and install the library to your local
Maven repository (i.e. `~/.m2/repository`).

Artifacts
---------

To add a React dependency to a Maven project, add the following to your
`pom.xml`:

    <repositories>
      <repository>
        <id>ooo-repo</id>
        <url>http://threerings.github.com/maven-repo</url>
      </repository>
    </repositories>
    <dependencies>
      <dependency>
        <groupId>com.threerings</groupId>
        <artifactId>react</artifactId>
        <version>1.0</version>
      </dependency>
    </dependencies>

To add it to an Ivy, SBT, or other Maven-repository-using build configuration,
simply remove the vast majority of the boilerplate above.

If you prefer to download a pre-built binary, that can be found here:

* [react-1.0.jar](http://threerings.github.com/maven-repo/com/threerings/react/1.0/react-1.0.jar)

GWT
---

React is also usable from [GWT](http://code.google.com/webtoolkit/). Add the
jar to your project per the above instructions and add the following to your
`.gwt.xml` file:

    <inherits name="react.React"/>

Distribution
------------

React is released under the New BSD License. The most recent version of the
library is available at http://github.com/threerings/react

Contact
-------

Questions, comments, and other communications should be directed to the [Three
Rings Libraries](http://groups.google.com/group/ooo-libs) Google Group.

[signal/slot]: http://en.wikipedia.org/wiki/Signals_and_slots
[functional reactive programming]: http://en.wikipedia.org/wiki/Functional_reactive_programming

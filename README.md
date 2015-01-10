React
=====

React is a low-level library that provides [signal/slot] and [functional reactive programming]-like
primitives. It can serve as the basis for a user interface toolkit, or any other library that has a
model on which clients will listen and to which they will react.

* [API docs](http://threerings.github.com/react/apidocs/) are available.

Building (Java)
---------------

The library is built using [SBT] or [Maven].

Invoke `sbt publish-local` to build and install the library to your local Ivy repository (i.e.
`~/.ivy2/local`).

Invoke `mvn install` to build and install the library to your local Maven repository (i.e.
`~/.m2/repository`).

Building (Objective-C)
----------------------

The Objective-C library is distributed as an Xcode project with no external dependencies. Add
`react.xcodeproj` to your project, add `/path/to/react/src/main/objc` to your user header search
paths, and add `#import "React.h"` to your project's pre-compiled header.

Artifacts
---------

To add a React dependency to a Maven project, add the following to your `pom.xml`:

    <dependencies>
      <dependency>
        <groupId>com.threerings</groupId>
        <artifactId>react</artifactId>
        <version>1.5</version>
      </dependency>
    </dependencies>

To add it to an Ivy, SBT, or other Maven-repository-using build configuration, simply remove the
vast majority of the boilerplate above.

If you prefer to download a pre-built binary, that can be found here:

* [react-1.5.jar](http://repo2.maven.org/maven2/com/threerings/react/1.5/react-1.5.jar)

GWT
---

React is also usable from [GWT](http://code.google.com/webtoolkit/). Add the jar to your project per
the above instructions and add the following to your `.gwt.xml` file:

    <inherits name="react"/>

Distribution
------------

React is released under the New BSD License. The most recent version of the library is available at
http://github.com/threerings/react

Contact
-------

Questions, comments, and other communications should be directed to the [Three Rings
Libraries](http://groups.google.com/group/ooo-libs) Google Group.

[signal/slot]: http://en.wikipedia.org/wiki/Signals_and_slots
[functional reactive programming]: http://en.wikipedia.org/wiki/Functional_reactive_programming
[SBT]: http://github.com/harrah/xsbt/wiki/Setup
[Maven]: http://maven.apache.org/

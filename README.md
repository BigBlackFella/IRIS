# IRIS

A runtime for Interaction Resources & Information Services

## Purpose

Quickly and easily create REST APIs for use by humans and machines.

The project allow you to:

* create web based services according to RESTful constraints
* aggregate / mashup multiple resource managers into a single interaction service
* program Hypermedia (HATEOAS) interactions with a state machine domain specific language (DSL)


## Requires (within Temenos)

* Configure settings.xml to add the following maven repository http://maven.oams.com/content/groups/all/
* Java JDK 1.7 (JDK 1.6 by skipping some integration test modules)
* Maven 3


## Requires (standalone)

* svn checkout http://cambridge.googlecode.com/svn/trunk/ cambridge-read-only
* https://github.com/lucasgut/halbuilder-core.git
* Java JDK 1.7 (JDK 1.6 by skipping some integration test modules)
* Maven 3


## Build commands:

`mvn clean` Clean projects

`mvn install` Build and install to maven repository

`mvn install -Ddebug` Build and install to maven repository without running integration tests

`mvn site` Build maven site

`mvn site:deploy` Deploy maven site [default: C:\temp\iris\site]



[![Javadocs](http://www.javadoc.io/badge/cc.redberry/libdivide4j.svg)](http://www.javadoc.io/doc/cc.redberry/libdivide4j) [![Build Status](https://travis-ci.org/PoslavskySV/libdivide4j.svg?branch=master)](https://travis-ci.org/PoslavskySV/libdivide4j)


# libdivide4j
Optimized integer division for Java

This is a Java fork of C/C++ libdivide library for fast integer division (http://libdivide.com and https://github.com/ridiculousfish/libdivide). 

Fast division by constant divisor is about 2-3 times faster then ordinary Java divide operation on most of modern CPUs.

__Requirements:__ Java 8 or higher

### Examples

The typical use case is when you have to use divide operation many times with a fixed divisor.
```java
    long[] someData = ...;
    long denominator = 45;
    FastDivision.Magic magic = FastDivision.magicSigned(denominator);

    long[] reduced = new long[someData.length];
    for (int i = 0; i < someData.length; ++i){
        // this is the same as someData[i] / denominator but 3 times faster
        reduced[i] = FastDivision.divideSignedFast(someData[i], magic);
    }
```

Library supports operations with unsigned integers:
```java
    // large unsigned modulus
    long largeModulus = Long.MAX_VALUE + 1;
    // some large number
    long someNum = Long.MAX_VALUE + 99999;
    FastDivision.Magic magic = FastDivision.magicUnsigned(denominator);

    // this will give 99998 (obviously)
    long reduced = FastDivision.modUnsignedFast(someNum, magic);
    assert reduced == 99998;
```


### Maven

Maven dependency:
```xml
    <dependency>
        <groupId>cc.redberry</groupId>
        <artifactId>libdivide4j</artifactId>
        <version>1.2</version>
    </dependency>
```        

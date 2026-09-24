# Custom Hash Function

A hash function built from scratch in Java.

## The idea

- Keeps a **32-byte state**, seeded from a fixed constant.
- Each input byte is **XORed** into the state at a position that cycles through all 32
  slots, then **multiplied** by a fixed constant.
- After all input is absorbed, a final mixing round XORs each byte with its neighboring bytes and multiplies it again. 
This helps mix the bytes further.
- The final state is printed as a **64-character hex string**.
## Input

Manual text is UTF-8 encoded. Files are hashed by their exact raw bytes. Nothing is
trimmed, case-changed, or otherwise normalized.

## Prerequisites

- Java (JDK, includes `javac`) installed
- Git, to clone this repo
## Setup

```bash
git clone https://github.com/RokasTverijonas/Blockchain-1.git
cd MyHash
```

## Run

Compile the source files:
```bash
javac Main.java MyHash.java
```

Hash a file:
```bash
java Main path/to/file.txt
```

Hash typed input:
```bash
java Main
```
 
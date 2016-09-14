# Forensic Utilities for Windows

These tools help identify and differentiate binary files.

> The goal of computer forensics is to examine digital media in a forensically sound manner with the aim of identifying, preserving, recovering, analyzing and presenting facts and opinions about the digital information.

From Wikipedia - [Computer Forensics](https://en.wikipedia.org/wiki/Computer_forensics)


## Useful Commands

# Example 1
Generate a name for the set of *.exe *.dll files in the current directory

dir /on /b *.exe *.dll|find /i /v "logical.dll"|codename.exe

# Example 2
Generate a name for the scpview.exe and its dependencies.

deps.exe scpview.exe|find /i /v "logical.dll"|FileInfo --sha1|codename.exe

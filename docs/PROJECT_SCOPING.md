# Project Scoping

Delphi codebases can often have indistinct boundaries, with common source files
being shared by multiple Delphi projects. There are a few ways of handling this from
a SonarDelphi perspective.

## Method 1: Having a different Sonar project for each `.dproj` file

This is the ideal project setup from an analysis quality standpoint.

Obviously, only do this for significantly different `.dproj` files - if your project has several
almost identical `.dproj` files to support multiple Delphi versions, for example, then it's
probably best to have a single project that includes only your latest `.dproj`.

## Method 2: Having one big Sonar project with multiple `.dproj` files in it

If SonarDelphi encounters multiple `.dproj` files during analysis, it will simply aggregate the values
from all of them together. In practice, this means that for every source file SonarDelphi would use every
search path and compiler define from the included `.dproj` files.

This is almost never the right thing to do, because conflicting configuration between `.dproj` files
usually renders the scan results unreliable.

Take this example:

```
MyDelphiCode/
  ProjectA/
    ProjectA.dproj
  ProjectB/
    ProjectB.dproj
  Common/
    CommonFile.pas
```

If `ProjectA.dproj` and `ProjectB.dproj` both include `CommonFile.pas`, there
are now two "versions" of `CommonFile.pas`: one that uses the search path, configuration,
and compiler defines of `ProjectA.dproj`, and another that uses those of `ProjectB.dproj`.

How can SonarDelphi reasonably come up with a "correct" interpretation of `CommonFile.pas` to analyze?
As a fallback, it makes a best-effort attempt by simply combining the settings from both `.dproj` files
together. Unless both `.dproj` files are set up very similarly, this is rarely the desired outcome.

In this case, `ProjectA` and `ProjectB` should be two separate Sonar projects. `CommonFile.pas` could be
handled in a few different ways:

| Possible Solution                                         | Pros                                                                          | Cons                                                           |
|-----------------------------------------------------------|-------------------------------------------------------------------------------|----------------------------------------------------------------|
| Include `CommonFile.pas` in both Sonar projects           | Both interpretations are scanned for issues                                   | Issue duplication between the two projects                     |
| Include `CommonFile.pas` in one of the Sonar projects     | One source of truth for raised issues                                         | Only scans one interpretation of the code                      |
| Add `CommonFile.pas` to a separate "Common" Sonar project | Can be freely customized to scan an authoritative version of `CommonFile.pas` | Doesn't reliably reflect either project, requires manual setup |

## Method 3: Don't rely on .dproj files at all

SonarDelphi lets you manually configure all of the settings it reads from `.dproj` files - see
[Project Options](CONFIGURATION.md#project-options).

If your Delphi codebase is arranged in such a way that scanning a `.dproj` does not make sense,
this is the only option. When configured correctly, this works just as well as Method 1 - it just
requires a little more care and maintenance.
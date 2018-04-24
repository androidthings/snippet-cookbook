# Android Things Snippet Cookbook

This repo contains sample code used to accomplish common tasks with
Android Things. These snippets are intended to be quick and easy recipes that
you can copy directly into your application.

## Contents

Each project directory contains the bare minimum code necessary to demonstrate
one or more tasks. Snippets are delimited regions in the source files using
special comments delimiters. Each delimiter contains the snippet id. Multiple
regions can be defined in a single file, referring to the same or to multiple
snippets. Delimited regions can overlap, as long as they refer to different
snippets.

Snippets ids, along with relevant metadata, have to be listed in the
`snippets-registry.json` file. Snippets not listed in the registry will be
ignored.

The snippet registry contains the following metadata about each snippet:
- `title`: Human readable name of the snippet
- `description`: One line description of the snippet
- `samples`: Array of URLs of fully-fledged samples demonstrating tasks similar
  to what the snippet demonstrates (optional)
- `category`: Framework subsystem or other logical grouping that describes the
  snippet's function (e.g. "Audio" or "Bluetooth").

All projects in this repo must be compilable and updated regularly.

The snippets in the registry will ultimately be featured on the [Android
Things Community Hub](https://androidthings.withgoogle.com).

## Delimiters

Delimiters must follow the syntax below:
```
// [START my_snippet]
code you want to include in my_snippet
// [START_EXCLUDE]
code that will be ignored and replaced by ellipsis
// [END_EXCLUDE]
more code you want to include in my_snippet
// [END my_snippet]
```

When processed, the file above will generate the following snippet:

```
code you want to include in my_snippet
// ...
more code you want to include in my_snippet
```

The delimiter line can have any indentation and it must be a valid single-line
comment in the syntax of the file language:
- `//` for Java, C, C++, Kotlin, Gradle
- `<!-- -->` for XML


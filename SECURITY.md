# Security policy

## Supported versions

Only the latest commit on `main` gets fixes. Tags such as `series1` and `agentic1` freeze the code a tutorial was written against and are not patched.

## Reporting a vulnerability

Please report it privately through GitHub: Security > Report a vulnerability. Do not open a public issue.

I read reports when I can, usually within a week. The server runs locally over stdio and only reads the Javadoc folder you point it at, so path handling in `get_doc` and resource use in the tool input schemas are the areas I care about most.

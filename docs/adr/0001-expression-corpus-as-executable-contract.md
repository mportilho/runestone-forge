# Use Expression Corpus as Executable Contract

We will store each expression case as one YAML file with controlled coverage tags and validate the corpus structurally in tests. This is more verbose than aggregated fixtures or ad hoc JUnit examples, but it gives stable case IDs, small diffs, and a shared behavioral contract for parser, semantic resolver, runtime, migration, and differential verification.

parallel-file-analyzer
======================

Tool which creates and processes files in parallel.

In contrast to [`SMP-Parallel-Execution-Framework`](https://github.com/kyledewey/SMP-Parallel-Execution-Framework), files can be created on the fly, which is more flexible.

Ultimately, this should replace [`SMP-Parallel-Execution-Framework`](https://github.com/kyledewey/SMP-Parallel-Execution-Framework).
This could have been written as an extension to [`SMP-Parallel-Execution-Framework`](https://github.com/kyledewey/SMP-Parallel-Execution-Framework), but that is so needlessly complex with mostly boilerplate that I want to start fresh.

**When the processing of a file has been completed, the original file is deleted.**

# Usage for a Static Set of Files #

Invoke like so:

```console
sbt "run-main parfile.StaticSMP config.txt file1 file2 file3"
```

...where `config.txt` is a configuration file, and files `file1`, `file2`, and `file3` are files to process.
The configuration file has a `key: value` format.  For example:

```
foo: bar
multi_space: this has multiple things
```

As for the legal configuration entries, they are described below:

<table border="1">
  <tr>
    <th>Key</th>
    <th>Description</th>
    <th>Default Value</th>
  </tr>
  
  <tr>
    <td><code>consumer_base_command</code></td>
    <td>Command to run on each file.  Assumed that the file will be the last parameter.</td>
    <td>N/A</td>
  </tr>
  
  <tr>
    <td><code>num_cores</code></td>
    <td>Number of cores to run on.  0 indicates the number of available cores.  Error if negative.</td>
    <td>0</td>
  </tr>
</table>


# Usage for a Dynamically Generated Set of Files #

Invoke like so:

```console
sbt "run-main parfile.DynamicSMP config.txt"
```

...where `config.txt` is a configuration file.
The configuration file has the same format as described in the section on usage for a static set of files.
As for the legal configuration entries, they are described below:

<table border="1">
  <tr>
    <th>Key</th>
    <th>Description</th>
    <th>Default Value</th>
  </tr>

  <tr>
    <td><code>producer_command</code></td>
    <td>
      Command to run to produce files.  It is assumed that the producer will actually write the files
      to disk, and then write the path of the written file to standard output, one file per line.
    </td>
    <td>N/A</td>
  </tr>

  <td>
    <td><code>num_producers</code></td>
    <td>
      The number of producer threads to run.  Note that each producer thread runs the same
      <code>producer_command</code>.  0 indicates the number of available cores.  Error if negative.
    </td>
    <td>1</td>
  </td>
  
  <tr>
    <td><code>consumer_save_file_to</code></td>
    <td>
      Directory where to save input files if the consumer generated any output (to either standard output
      or standard error) while processing the given input file.  By convention, it is assumed that if
      any output was generated, then there must have been something wrong with the processing of the file,
      and so it is saved for later debugging purposes.
    </td>
    <td>N/A</td>
  </tr>
  
  <tr>
    <td><code>consumer_base_command</code></td>
    <td>Command to run on each file.  Assumed that the file will be the last parameter.</td>
    <td>N/A</td>
  </tr>

  <tr>
    <td><code>num_consumers</code></td>
    <td>
      Number of consumers to run.  Again, each runs the same <code>consumer_base_command</code>.
      0 indicates the number of available cores.  Error if negative.
    </td>
    <td>0</td>
  </tr>
</table>


using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.IO;
using System.Linq;

static class Program
{
    static int Usage()
    {
        Console.WriteLine(@"Modules.exe lists the modules loaded in a process
Usage:

modules <proccesId1> <proccesId1> ...

Examples:

modules 1234
modules 1234 0xABCD 0xBEEF

PIDs may be decimal or hexadecimal.  Hex PIDs must begin with `0x` or `&h`.
");
        return 1;
    }

    static int ParseProcessId(string pid)
    {
        return pid.StartsWith("0x") || pid.StartsWith("&h")
            ? int.Parse(pid, NumberStyles.AllowHexSpecifier)
            : int.Parse(pid);
    }

    static Process GetProcess(string pid)
    {
        try
        {
            int id = ParseProcessId(pid);
            return Process.GetProcessById(id);
        }
        catch (FormatException nfe)
        {
            Console.Error.WriteLine("Trouble parsing pid:" + pid + ".  " + nfe.Message);
            return null;
        }
    }

    static int Main(string[] args)
    {
        if (!args.Any() || args.Any(new HashSet<string> {"/?", "-?", "--help"}.Contains))
            return Usage();

        var modules = new HashSet<string>(
            args
                .Select(GetProcess)
                .SelectMany(p => p.Modules.Cast<ProcessModule>())
                .Select(m => m.FileName.Normalized()));

        PrintModules(modules);

        return 0;
    }

    static void PrintModules(IEnumerable<string> filepaths)
    {
        var x = filepaths.OrderBy(p => Path.GetFileName(p));
        var cd = Environment.CurrentDirectory.Normalized() + "\\";
        foreach (var p in x)
        {
            Console.Error.WriteLine("     raw         " + p);
            Console.Error.WriteLine("     normal      " + p.Normalized());
            var pretty = p.StartsWith(cd) ? p.Substring(cd.Length) : p;
            Console.WriteLine(pretty);
        }
    }

    static string Normalized(this string s)
    {
        return s == null ? "" : s.ToLower();
    }
}

pushd %1
call archive.cmd
popd
move /y %1\.about.txt %1.about.txt
move /y %1\*.7z

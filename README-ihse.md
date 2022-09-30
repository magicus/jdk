We assume a libjvm directory containing:
"gtest" directory (optional), will be copied to images/test/hotspot/gtest/$VARIANT
main build hotspot will be put first in jvm.cfg
we will copy in dll/lib*.so/lib*.dylib into jdk/lib/$VARIANT.
Also debug symbols.
A file named jvm-features.txt which contain the list of features which is needed
for CDS.
A file named jvm-variant.txt which contain the name of the variant.

And create symlink $VARIANT/libjsig.dylib -> ../libjsig.dylib

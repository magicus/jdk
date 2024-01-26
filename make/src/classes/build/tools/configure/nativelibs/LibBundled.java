package build.tools.configure.nativelibs;

public class LibBundled {
    // libjpeg
    /*
    AC_CHECK_HEADER(jpeglib.h, [],
        [ AC_MSG_ERROR([--with-libjpeg=system specified, but jpeglib.h not found!])])
    AC_CHECK_LIB(jpeg, jpeg_CreateDecompress, [],
        [ AC_MSG_ERROR([--with-libjpeg=system specified, but no libjpeg found])])

     */


    // giflib -- no pkg-config!

/*
    AC_CHECK_HEADER(gif_lib.h, [],
        [ AC_MSG_ERROR([--with-giflib=system specified, but gif_lib.h not found!])])
    AC_CHECK_LIB(gif, DGifGetCode, [],
        [ AC_MSG_ERROR([--with-giflib=system specified, but no giflib found!])])

 */

    // libpng
    /*
    no check?
     */

    // zlib
    /*
  AC_CHECK_LIB(z, compress,
      [ ZLIB_FOUND=yes ],
      [ ZLIB_FOUND=no ])


     */
    // lcms == lcms2
    // harfbuzz
}

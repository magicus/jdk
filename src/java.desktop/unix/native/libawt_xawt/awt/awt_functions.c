/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

 #include <dlfcn.h>

#include <X11/Xlib.h>
#include <X11/XKBlib.h>
#include <X11/Xutil.h>
#include <X11/extensions/shape.h>
#include <X11/extensions/Xdbe.h>
#include <X11/extensions/XInput.h>
#include <X11/extensions/Xrender.h>
#include <X11/extensions/XShm.h>
#include <X11/extensions/XTest.h>

#include "jni.h"

#if NeedWidePrototypes
  #define KEY_CODE_TYPE unsigned int
#else
  #define KEY_CODE_TYPE KeyCode
#endif

#define MAX_LIBS 5
#define MAX_SYMBOLS 176

typedef struct {
    const char* symbol_name;
    void**      symbol_ptr;
} SymbolInfo;

typedef SymbolInfo SymbolArray[MAX_SYMBOLS];

typedef struct {
    const char*  lib_name;
    const char*  lib_filename;
    SymbolArray* lib_symbols;
    int*         lib_symbol_count;
} LibInfo;

static LibInfo all_libs[MAX_LIBS];
static int lib_count = 0;

#define REGISTER_LIB(libname)                                                    \
    static SymbolArray lib##libname##_symbols;                                   \
    static int lib##libname##_symbol_count = 0;                                  \
    __attribute__((constructor(1010))) static void register_lib_##libname() {    \
        if (lib_count < MAX_LIBS) {                                              \
            all_libs[lib_count++] = (LibInfo){ #libname, "lib"#libname".so",     \
             &lib##libname##_symbols, &lib##libname##_symbol_count };            \
        } else {                                                                 \
            fprintf(stderr, "Internal error: Too many libs at: %s\n", #libname); \
        }                                                                        \
    }

#define REGISTER_SYMBOL_INNER(libname, funcname, rettype, params, args, ret)     \
  typedef rettype (* funcname##_t)params;                                        \
  static funcname##_t funcname##_fn;                                             \
  rettype funcname params {                                                      \
    ret funcname##_fn args;                                                      \
  }                                                                              \
  __attribute__((constructor(1020))) static void register_symbol_##funcname() {  \
    if (lib##libname##_symbol_count < MAX_SYMBOLS) {                             \
      lib##libname##_symbols[lib##libname##_symbol_count++] =                    \
        (SymbolInfo) { #funcname, (void**)&funcname##_fn };                      \
    } else {                                                                     \
      fprintf(stderr, "Internal error: Too many symbols at: %s\n", #funcname);   \
    }                                                                            \
  }

#define REGISTER_SYMBOL(libname, funcname, rettype, params, args) \
 REGISTER_SYMBOL_INNER(libname, funcname, rettype, params, args, return)

#define REGISTER_SYMBOL_NO_RETURN(libname, funcname, params, args) \
 REGISTER_SYMBOL_INNER(libname, funcname, void, params, args, )

// Register all native libraries that we need to load symbols from
REGISTER_LIB(X11)
REGISTER_LIB(Xext)
REGISTER_LIB(Xi)
REGISTER_LIB(Xrender)
REGISTER_LIB(Xtst)

// Functions declared in Xlib.h and implemented in libX11.so
REGISTER_SYMBOL(X11, XAllocColor, Status, (Display* display, Colormap colormap, XColor* screen_in_out), (display, colormap, screen_in_out))
REGISTER_SYMBOL(X11, XAllocColorCells, Status, (Display* display, Colormap colormap, Bool contig, unsigned long* plane_masks_return, unsigned int nplanes, unsigned long* pixels_return, unsigned int npixels), (display, colormap, contig, plane_masks_return, nplanes, pixels_return, npixels))
REGISTER_SYMBOL(X11, XBell, int, (Display* display, int percent), (display, percent))
REGISTER_SYMBOL(X11, XChangeActivePointerGrab, int, (Display* display, unsigned int event_mask, Cursor cursor, Time time), (display, event_mask, cursor, time))
REGISTER_SYMBOL(X11, XChangeGC, int, (Display* display, GC gc, unsigned long valuemask, XGCValues* values), (display, gc, valuemask, values))
REGISTER_SYMBOL(X11, XChangeProperty, int, (Display* display, Window w, Atom property, Atom type, int format, int mode, _Xconst unsigned char* data, int nelements), (display, w, property, type, format, mode, data, nelements))
REGISTER_SYMBOL(X11, XChangeWindowAttributes, int, (Display* display, Window w, unsigned long valuemask, XSetWindowAttributes* attributes), (display, w, valuemask, attributes))
REGISTER_SYMBOL(X11, XCheckIfEvent, Bool, (Display* display, XEvent* event_return, Bool (*predicate)(Display*, XEvent*, XPointer), XPointer arg), (display, event_return, predicate, arg))
REGISTER_SYMBOL(X11, XClearWindow, int, (Display* display, Window w), (display, w))
REGISTER_SYMBOL(X11, XCloseDisplay, int, (Display* display), (display))
REGISTER_SYMBOL(X11, XConfigureWindow, int, (Display* display, Window w, unsigned int value_mask, XWindowChanges* values), (display, w, value_mask, values))
REGISTER_SYMBOL(X11, XConvertSelection, int, (Display* display, Atom selection, Atom target, Atom property, Window requestor, Time time), (display, selection, target, property, requestor, time))
REGISTER_SYMBOL(X11, XCopyArea, int, (Display* display, Drawable src, Drawable dest, GC gc, int src_x, int src_y, unsigned int width, unsigned int height, int dest_x, int dest_y), (display, src, dest, gc, src_x, src_y, width, height, dest_x, dest_y))
REGISTER_SYMBOL(X11, XCreateBitmapFromData, Pixmap, (Display* display, Drawable d, _Xconst char* data, unsigned int width, unsigned int height), (display, d, data, width, height))
REGISTER_SYMBOL(X11, XCreateColormap, Colormap, (Display* display, Window w, Visual* visual, int alloc), (display, w, visual, alloc))
REGISTER_SYMBOL(X11, XCreateFontCursor, Cursor, (Display* display, unsigned int shape), (display, shape))
REGISTER_SYMBOL(X11, XCreateFontSet, XFontSet, (Display* display, _Xconst char* base_font_name_list, char*** missing_charset_list, int* missing_charset_count, char** def_string), (display, base_font_name_list, missing_charset_list, missing_charset_count, def_string))
REGISTER_SYMBOL(X11, XCreateGC, GC, (Display* display, Drawable d, unsigned long valuemask, XGCValues* values), (display, d, valuemask, values))
REGISTER_SYMBOL(X11, XCreateIC, XIC, (XIM im, ...), (im))
REGISTER_SYMBOL(X11, XCreateImage, XImage*, (Display* display, Visual* visual, unsigned int depth, int format, int offset, char* data, unsigned int width, unsigned int height, int bitmap_pad, int bytes_per_line), (display, visual, depth, format, offset, data, width, height, bitmap_pad, bytes_per_line))
REGISTER_SYMBOL(X11, XCreatePixmap, Pixmap, (Display* display, Drawable d, unsigned int width, unsigned int height, unsigned int depth), (display, d, width, height, depth))
REGISTER_SYMBOL(X11, XCreatePixmapCursor, Cursor, (Display* display, Pixmap source, Pixmap mask, XColor* foreground_color, XColor* background_color, unsigned int x, unsigned int y), (display, source, mask, foreground_color, background_color, x, y))
REGISTER_SYMBOL(X11, XCreateWindow, Window, (Display* display, Window parent, int x, int y, unsigned int width, unsigned int height, unsigned int border_width, int depth, unsigned int class, Visual* visual, unsigned long valuemask, XSetWindowAttributes* attributes), (display, parent, x, y, width, height, border_width, depth, class, visual, valuemask, attributes))
REGISTER_SYMBOL(X11, XDeleteProperty, int, (Display* display, Window w, Atom property), (display, w, property))
REGISTER_SYMBOL_NO_RETURN(X11, XDestroyIC, (XIC ic), (ic))
REGISTER_SYMBOL(X11, XDestroyWindow, int, (Display* display, Window w), (display, w))
REGISTER_SYMBOL(X11, XDisplayKeycodes, int, (Display* display, int* min_keycodes_return, int* max_keycodes_return), (display, min_keycodes_return, max_keycodes_return))
REGISTER_SYMBOL(X11, XDisplayName, char*, (_Xconst char* string), (string))
REGISTER_SYMBOL(X11, XDisplayString, char*, (Display* display), (display))
REGISTER_SYMBOL(X11, XDrawArc, int, (Display* display, Drawable d, GC gc, int x, int y, unsigned int width, unsigned int height, int angle1, int angle2), (display, d, gc, x, y, width, height, angle1, angle2))
REGISTER_SYMBOL(X11, XDrawLine, int, (Display* display, Drawable d, GC gc, int x1, int y1, int x2, int y2), (display, d, gc, x1, y1, x2, y2))
REGISTER_SYMBOL(X11, XDrawLines, int, (Display* display, Drawable d, GC gc, XPoint* points, int npoints, int mode), (display, d, gc, points, npoints, mode))
REGISTER_SYMBOL(X11, XDrawRectangle, int, (Display* display, Drawable d, GC gc, int x, int y, unsigned int width, unsigned int height), (display, d, gc, x, y, width, height))
REGISTER_SYMBOL(X11, XDrawString, int, (Display* display, Drawable d, GC gc, int x, int y, _Xconst char* string, int length), (display, d, gc, x, y, string, length))
REGISTER_SYMBOL(X11, XDrawString16, int, (Display* display, Drawable d, GC gc, int x, int y, _Xconst XChar2b* string, int length), (display, d, gc, x, y, string, length))
REGISTER_SYMBOL(X11, XEventsQueued, int, (Display* display, int mode), (display, mode))
REGISTER_SYMBOL(X11, XFillArc, int, (Display* display, Drawable d, GC gc, int x, int y, unsigned int width, unsigned int height, int angle1, int angle2), (display, d, gc, x, y, width, height, angle1, angle2))
REGISTER_SYMBOL(X11, XFillPolygon, int, (Display* display, Drawable d, GC gc, XPoint* points, int npoints, int shape, int mode), (display, d, gc, points, npoints, shape, mode))
REGISTER_SYMBOL(X11, XFillRectangle, int, (Display* display, Drawable d, GC gc, int x, int y, unsigned int width, unsigned int height), (display, d, gc, x, y, width, height))
REGISTER_SYMBOL(X11, XFillRectangles, int, (Display* display, Drawable d, GC gc, XRectangle* rectangles, int nrectangles), (display, d, gc, rectangles, nrectangles))
REGISTER_SYMBOL(X11, XFilterEvent, Bool, (XEvent* event, Window window), (event, window))
REGISTER_SYMBOL(X11, XFlush, int, (Display* display), (display))
REGISTER_SYMBOL(X11, XFree, int, (void* data), (data))
REGISTER_SYMBOL(X11, XFreeColormap, int, (Display* display, Colormap colormap), (display, colormap))
REGISTER_SYMBOL(X11, XFreeColors, int, (Display* display, Colormap colormap, unsigned long* pixels, int npixels, unsigned long planes), (display, colormap, pixels, npixels, planes))
REGISTER_SYMBOL(X11, XFreeCursor, int, (Display* display, Cursor cursor), (display, cursor))
REGISTER_SYMBOL(X11, XFreeFont, int, (Display* display, XFontStruct* font_struct), (display, font_struct))
REGISTER_SYMBOL(X11, XFreeFontNames, int, (char** list), (list))
REGISTER_SYMBOL(X11, XFreeFontPath, int, (char** list), (list))
REGISTER_SYMBOL_NO_RETURN(X11, XFreeFontSet, (Display* display, XFontSet font_set), (display, font_set))
REGISTER_SYMBOL(X11, XFreeGC, int, (Display* display, GC gc), (display, gc))
REGISTER_SYMBOL(X11, XFreeModifiermap, int, (XModifierKeymap* modmap), (modmap))
REGISTER_SYMBOL(X11, XFreePixmap, int, (Display* display, Pixmap pixmap), (display, pixmap))
REGISTER_SYMBOL_NO_RETURN(X11, XFreeStringList, (char** list), (list))
REGISTER_SYMBOL(X11, XGetAtomName, char*, (Display* display, Atom atom), (display, atom))
REGISTER_SYMBOL(X11, XGetDefault, char*, (Display* display, _Xconst char* program, _Xconst char* option), (display, program, option))
REGISTER_SYMBOL(X11, XGetErrorDatabaseText, int, (Display* display, _Xconst char* name, _Xconst char* message, _Xconst char* default_string, char* buffer_return, int length), (display, name, message, default_string, buffer_return, length))
REGISTER_SYMBOL(X11, XGetErrorText, int, (Display* display, int code, char* buffer_return, int length), (display, code, buffer_return, length))
REGISTER_SYMBOL(X11, XGetFontPath, char**, (Display* display, int* npaths_return), (display, npaths_return))
REGISTER_SYMBOL(X11, XGetGeometry, Status, (Display* display, Drawable d, Window* root_return, int* x_return, int* y_return, unsigned int* width_return, unsigned int* height_return, unsigned int* border_width_return, unsigned int* depth_return), (display, d, root_return, x_return, y_return, width_return, height_return, border_width_return, depth_return))
REGISTER_SYMBOL(X11, XGetICValues, char*, (XIC ic, ...), (ic))
REGISTER_SYMBOL(X11, XGetImage, XImage*, (Display* display, Drawable d, int x, int y, unsigned int width, unsigned int height, unsigned long plane_mask, int format), (display, d, x, y, width, height, plane_mask, format))
REGISTER_SYMBOL(X11, XGetIMValues, char*, (XIM im, ...), (im))
REGISTER_SYMBOL(X11, XGetInputFocus, int, (Display* display, Window* focus_return, int* revert_to_return), (display, focus_return, revert_to_return))
REGISTER_SYMBOL(X11, XGetKeyboardMapping, KeySym*, (Display* display, KEY_CODE_TYPE first_keycode, int keycode_count, int* keysyms_per_keycode_return), (display, first_keycode, keycode_count, keysyms_per_keycode_return))
REGISTER_SYMBOL(X11, XGetModifierMapping, XModifierKeymap*, (Display* display), (display))
REGISTER_SYMBOL(X11, XGetPointerMapping, int, (Display* display, unsigned char* map_return, int nmap), (display, map_return, nmap))
REGISTER_SYMBOL(X11, XGetSelectionOwner, Window, (Display* display, Atom selection), (display, selection))
REGISTER_SYMBOL(X11, XGetWindowAttributes, Status, (Display* display, Window w, XWindowAttributes* window_attributes_return), (display, w, window_attributes_return))
REGISTER_SYMBOL(X11, XGetWindowProperty, int, (Display* display, Window w, Atom property, long long_offset, long long_length, Bool delete, Atom req_type, Atom* actual_type_return, int* actual_format_return, unsigned long* nitems_return, unsigned long* bytes_after_return, unsigned char** prop_return), (display, w, property, long_offset, long_length, delete, req_type, actual_type_return, actual_format_return, nitems_return, bytes_after_return, prop_return))
REGISTER_SYMBOL(X11, XGrabKeyboard, int, (Display* display, Window grab_window, Bool owner_events, int pointer_mode, int keyboard_mode, Time time), (display, grab_window, owner_events, pointer_mode, keyboard_mode, time))
REGISTER_SYMBOL(X11, XGrabPointer, int, (Display* display, Window grab_window, Bool owner_events, unsigned int event_mask, int pointer_mode, int keyboard_mode, Window confine_to, Cursor cursor, Time time), (display, grab_window, owner_events, event_mask, pointer_mode, keyboard_mode, confine_to, cursor, time))
REGISTER_SYMBOL(X11, XGrabServer, int, (Display* display), (display))
REGISTER_SYMBOL(X11, XIconifyWindow, Status, (Display* display, Window w, int screen_number), (display, w, screen_number))
REGISTER_SYMBOL(X11, XInternAtom, Atom, (Display* display, _Xconst char* atom_name, Bool only_if_exists), (display, atom_name, only_if_exists))
REGISTER_SYMBOL(X11, XInternAtoms, Status, (Display* dpy, char** names, int count, Bool onlyIfExists, Atom* atoms_return), (dpy, names, count, onlyIfExists, atoms_return))
REGISTER_SYMBOL(X11, XKeysymToKeycode, KeyCode, (Display* display, KeySym keysym), (display, keysym))
REGISTER_SYMBOL(X11, XListFonts, char**, (Display* display, _Xconst char* pattern, int maxnames, int* actual_count_return), (display, pattern, maxnames, actual_count_return))
REGISTER_SYMBOL(X11, XListPixmapFormats, XPixmapFormatValues*, (Display* display, int* count_return), (display, count_return))
REGISTER_SYMBOL(X11, XLoadQueryFont, XFontStruct*, (Display* display, _Xconst char* name), (display, name))
REGISTER_SYMBOL(X11, XLowerWindow, int, (Display* display, Window w), (display, w))
REGISTER_SYMBOL(X11, XMapRaised, int, (Display* display, Window w), (display, w))
REGISTER_SYMBOL(X11, XMapWindow, int, (Display* display, Window w), (display, w))
REGISTER_SYMBOL(X11, XMaskEvent, int, (Display* display, long event_mask, XEvent* event_return), (display, event_mask, event_return))
REGISTER_SYMBOL(X11, XMaxRequestSize, long, (Display* display), (display))
REGISTER_SYMBOL(X11, XmbDrawString, void, (Display* display, Drawable d, XFontSet font_set, GC gc, int x, int y, _Xconst char* text, int bytes_text), (display, d, font_set, gc, x, y, text, bytes_text))
REGISTER_SYMBOL(X11, XmbLookupString, int, (XIC ic, XKeyPressedEvent* event, char* buffer_return, int bytes_buffer, KeySym* keysym_return, Status* status_return), (ic, event, buffer_return, bytes_buffer, keysym_return, status_return))
REGISTER_SYMBOL(X11, XmbResetIC, char*, (XIC ic), (ic))
REGISTER_SYMBOL(X11, XMoveResizeWindow, int, (Display* display, Window w, int x, int y, unsigned int width, unsigned int height), (display, w, x, y, width, height))
REGISTER_SYMBOL(X11, XMoveWindow, int, (Display* display, Window w, int x, int y), (display, w, x, y))
REGISTER_SYMBOL(X11, XNextEvent, int, (Display* display, XEvent* event_return), (display, event_return))
REGISTER_SYMBOL(X11, XOpenDisplay, Display*, (_Xconst char* display_name), (display_name))
REGISTER_SYMBOL(X11, XOpenIM, XIM, (Display* dpy, struct _XrmHashBucketRec* rdb, char* res_name, char* res_class), (dpy, rdb, res_name, res_class))
REGISTER_SYMBOL(X11, XPeekEvent, int, (Display* display, XEvent* event_return), (display, event_return))
REGISTER_SYMBOL(X11, XPutBackEvent, int, (Display* display, XEvent* event), (display, event))
REGISTER_SYMBOL(X11, XPutImage, int, (Display* display, Drawable d, GC gc, XImage* image, int src_x, int src_y, int dest_x, int dest_y, unsigned int width, unsigned int height), (display, d, gc, image, src_x, src_y, dest_x, dest_y, width, height))
REGISTER_SYMBOL(X11, XQueryBestCursor, Status, (Display* display, Drawable d, unsigned int width, unsigned int height, unsigned int* width_return, unsigned int* height_return), (display, d, width, height, width_return, height_return))
REGISTER_SYMBOL(X11, XQueryColors, int, (Display* display, Colormap colormap, XColor* defs_in_out, int ncolors), (display, colormap, defs_in_out, ncolors))
REGISTER_SYMBOL(X11, XQueryExtension, Bool, (Display* display, _Xconst char* name, int* major_opcode_return, int* first_event_return, int* first_error_return), (display, name, major_opcode_return, first_event_return, first_error_return))
REGISTER_SYMBOL(X11, XQueryKeymap, int, (Display* display, char keys_return[32]), (display, keys_return))
REGISTER_SYMBOL(X11, XQueryPointer, Bool, (Display* display, Window w, Window* root_return, Window* child_return, int* root_x_return, int* root_y_return, int* win_x_return, int* win_y_return, unsigned int* mask_return), (display, w, root_return, child_return, root_x_return, root_y_return, win_x_return, win_y_return, mask_return))
REGISTER_SYMBOL(X11, XQueryTextExtents16, int, (Display* display, XID font_ID, _Xconst XChar2b* string, int nchars, int* direction_return, int* font_ascent_return, int* font_descent_return, XCharStruct* overall_return), (display, font_ID, string, nchars, direction_return, font_ascent_return, font_descent_return, overall_return))
REGISTER_SYMBOL(X11, XQueryTree, Status, (Display* display, Window w, Window* root_return, Window* parent_return, Window** children_return, unsigned int* nchildren_return), (display, w, root_return, parent_return, children_return, nchildren_return))
REGISTER_SYMBOL(X11, XRaiseWindow, int, (Display* display, Window w), (display, w))
REGISTER_SYMBOL(X11, XRefreshKeyboardMapping, int, (XMappingEvent* event_map), (event_map))
REGISTER_SYMBOL(X11, XRegisterIMInstantiateCallback, Bool, (Display* dpy, struct _XrmHashBucketRec* rdb, char* res_name, char* res_class, XIDProc callback, XPointer client_data), (dpy, rdb, res_name, res_class, callback, client_data))
REGISTER_SYMBOL(X11, XReparentWindow, int, (Display* display, Window w, Window parent, int x, int y), (display, w, parent, x, y))
REGISTER_SYMBOL(X11, XResizeWindow, int, (Display* display, Window w, unsigned int width, unsigned int height), (display, w, width, height))
REGISTER_SYMBOL(X11, XRestackWindows, int, (Display* display, Window* windows, int nwindows), (display, windows, nwindows))
REGISTER_SYMBOL(X11, XRootWindow, Window, (Display* display, int screen_number), (display, screen_number))
REGISTER_SYMBOL(X11, XScreenCount, int, (Display* display), (display))
REGISTER_SYMBOL(X11, XScreenNumberOfScreen, int, (Screen* screen), (screen))
REGISTER_SYMBOL(X11, XSelectInput, int, (Display* display, Window w, long event_mask), (display, w, event_mask))
REGISTER_SYMBOL(X11, XSendEvent, Status, (Display* display, Window w, Bool propagate, long event_mask, XEvent* event_send), (display, w, propagate, event_mask, event_send))
REGISTER_SYMBOL(X11, XSetBackground, int, (Display* display, GC gc, unsigned long background), (display, gc, background))
REGISTER_SYMBOL(X11, XSetClipMask, int, (Display* display, GC gc, Pixmap pixmap), (display, gc, pixmap))
REGISTER_SYMBOL(X11, XSetClipOrigin, int, (Display* display, GC gc, int clip_x_origin, int clip_y_origin), (display, gc, clip_x_origin, clip_y_origin))
REGISTER_SYMBOL(X11, XSetClipRectangles, int, (Display* display, GC gc, int clip_x_origin, int clip_y_origin, XRectangle* rectangles, int n, int ordering), (display, gc, clip_x_origin, clip_y_origin, rectangles, n, ordering))
REGISTER_SYMBOL(X11, XSetCloseDownMode, int, (Display* display, int close_mode), (display, close_mode))
REGISTER_SYMBOL(X11, XSetErrorHandler, XErrorHandler, (XErrorHandler handler), (handler))
REGISTER_SYMBOL(X11, XSetFillStyle, int, (Display* display, GC gc, int fill_style), (display, gc, fill_style))
REGISTER_SYMBOL(X11, XSetFont, int, (Display* display, GC gc, Font font), (display, gc, font))
REGISTER_SYMBOL(X11, XSetForeground, int, (Display* display, GC gc, unsigned long foreground), (display, gc, foreground))
REGISTER_SYMBOL(X11, XSetFunction, int, (Display* display, GC gc, int function), (display, gc, function))
REGISTER_SYMBOL(X11, XSetGraphicsExposures, int, (Display* display, GC gc, Bool graphics_exposures), (display, gc, graphics_exposures))
REGISTER_SYMBOL_NO_RETURN(X11, XSetICFocus, (XIC ic), (ic))
REGISTER_SYMBOL(X11, XSetICValues, char*, (XIC ic, ...), (ic))
REGISTER_SYMBOL(X11, XSetIMValues, char*, (XIM im, ...), (im))
REGISTER_SYMBOL(X11, XSetInputFocus, int, (Display* display, Window focus, int revert_to, Time time), (display, focus, revert_to, time))
REGISTER_SYMBOL(X11, XSetIOErrorHandler, XIOErrorHandler, (XIOErrorHandler handler), (handler))
REGISTER_SYMBOL(X11, XSetLocaleModifiers, char*, (const char* modifier_list), (modifier_list))
REGISTER_SYMBOL(X11, XSetSelectionOwner, int, (Display* display, Atom selection, Window owner, Time time), (display, selection, owner, time))
REGISTER_SYMBOL(X11, XSetTransientForHint, int, (Display* display, Window w, Window prop_window), (display, w, prop_window))
REGISTER_SYMBOL(X11, XSetWindowBackground, int, (Display* display, Window w, unsigned long background_pixel), (display, w, background_pixel))
REGISTER_SYMBOL(X11, XSetWindowBackgroundPixmap, int, (Display* display, Window w, Pixmap background_pixmap), (display, w, background_pixmap))
REGISTER_SYMBOL(X11, XSupportsLocale, Bool, (void), ())
REGISTER_SYMBOL(X11, XSync, int, (Display* display, Bool discard), (display, discard))
REGISTER_SYMBOL(X11, XTranslateCoordinates, Bool, (Display* display, Window src_w, Window dest_w, int src_x, int src_y, int* dest_x_return, int* dest_y_return, Window* child_return), (display, src_w, dest_w, src_x, src_y, dest_x_return, dest_y_return, child_return))
REGISTER_SYMBOL(X11, XUngrabKeyboard, int, (Display* display, Time time), (display, time))
REGISTER_SYMBOL(X11, XUngrabPointer, int, (Display* display, Time time), (display, time))
REGISTER_SYMBOL(X11, XUngrabServer, int, (Display* display), (display))
REGISTER_SYMBOL(X11, XUnmapWindow, int, (Display* display, Window w), (display, w))
REGISTER_SYMBOL_NO_RETURN(X11, XUnsetICFocus, (XIC ic), (ic))
REGISTER_SYMBOL(X11, XVaCreateNestedList, XVaNestedList, (int unused, ...), (unused))
REGISTER_SYMBOL(X11, XVisualIDFromVisual, VisualID, (Visual* visual), (visual))
REGISTER_SYMBOL(X11, XWarpPointer, int, (Display* display, Window src_w, Window dest_w, int src_x, int src_y, unsigned int src_width, unsigned int src_height, int dest_x, int dest_y), (display, src_w, dest_w, src_x, src_y, src_width, src_height, dest_x, dest_y))
REGISTER_SYMBOL(X11, XWindowEvent, int, (Display* display, Window w, long event_mask, XEvent* event_return), (display, w, event_mask, event_return))

// Functions declared in XKBlib.h and implemented in libX11.so
REGISTER_SYMBOL(X11, XkbFreeKeyboard, void, (XkbDescPtr xkb, unsigned int which, Bool freeDesc), (xkb, which, freeDesc))
REGISTER_SYMBOL(X11, XkbGetMap, XkbDescPtr, (Display* dpy, unsigned int which, unsigned int deviceSpec), (dpy, which, deviceSpec))
REGISTER_SYMBOL(X11, XkbGetState, Status, (Display* dpy, unsigned int deviceSpec, XkbStatePtr rtrnState), (dpy, deviceSpec, rtrnState))
REGISTER_SYMBOL(X11, XkbGetUpdatedMap, Status, (Display* dpy, unsigned int which, XkbDescPtr desc), (dpy, which, desc))
REGISTER_SYMBOL(X11, XkbIgnoreExtension, Bool, (Bool ignore), (ignore))
REGISTER_SYMBOL(X11, XkbKeycodeToKeysym, KeySym, (Display* dpy, KEY_CODE_TYPE kc, int group, int level), (dpy, kc, group, level))
REGISTER_SYMBOL(X11, XkbLibraryVersion, Bool, (int* libMajorRtrn, int* libMinorRtrn), (libMajorRtrn, libMinorRtrn))
REGISTER_SYMBOL(X11, XkbQueryExtension, Bool, (Display* dpy, int* opcodeReturn, int* eventBaseReturn, int* errorBaseReturn, int* majorRtrn, int* minorRtrn), (dpy, opcodeReturn, eventBaseReturn, errorBaseReturn, majorRtrn, minorRtrn))
REGISTER_SYMBOL(X11, XkbSelectEventDetails, Bool, (Display* dpy, unsigned int deviceID, unsigned int eventType, unsigned long affect, unsigned long details), (dpy, deviceID, eventType, affect, details))
REGISTER_SYMBOL(X11, XkbSelectEvents, Bool, (Display* dpy, unsigned int deviceID, unsigned int affect, unsigned int values), (dpy, deviceID, affect, values))
REGISTER_SYMBOL(X11, XkbSetDetectableAutoRepeat, Bool, (Display* dpy, Bool detectable, Bool* supported), (dpy, detectable, supported))
REGISTER_SYMBOL(X11, XkbTranslateKeyCode, Bool, (XkbDescPtr xkb, KeyCode keycode, unsigned int modifiers, unsigned int* modifiers_return, KeySym* keysym_return), (xkb, keycode, modifiers, modifiers_return, keysym_return))

// Functions declared in Xutil.h and implemented in libX11.so
REGISTER_SYMBOL(X11, XAllocSizeHints, XSizeHints*, (void), ())
REGISTER_SYMBOL(X11, XAllocWMHints, XWMHints*, (void), ())
REGISTER_SYMBOL(X11, XConvertCase, void, (KeySym sym, KeySym* lower, KeySym* upper), (sym, lower, upper))
REGISTER_SYMBOL(X11, XCreateRegion, Region, (void), ())
REGISTER_SYMBOL(X11, XDestroyRegion, int, (Region r), (r))
REGISTER_SYMBOL(X11, XEmptyRegion, int, (Region r), (r))
REGISTER_SYMBOL(X11, XGetIconSizes, Status, (Display* display, Window w, XIconSize** size_list_return, int* count_return), (display, w, size_list_return, count_return))
REGISTER_SYMBOL(X11, XGetVisualInfo, XVisualInfo*, (Display* display, long vinfo_mask, XVisualInfo* vinfo_template, int* nitems_return), (display, vinfo_mask, vinfo_template, nitems_return))
REGISTER_SYMBOL(X11, XGetWMHints, XWMHints*, (Display* display, Window w), (display, w))
REGISTER_SYMBOL(X11, XGetWMNormalHints, Status, (Display* display, Window w, XSizeHints* hints_return, long* supplied_return), (display, w, hints_return, supplied_return))
REGISTER_SYMBOL(X11, XIntersectRegion, int, (Region sra, Region srb, Region dr_return), (sra, srb, dr_return))
REGISTER_SYMBOL(X11, XSetWMHints, int, (Display* display, Window w, XWMHints* wm_hints), (display, w, wm_hints))
REGISTER_SYMBOL(X11, XSetWMNormalHints, void, (Display* display, Window w, XSizeHints* hints), (display, w, hints))
REGISTER_SYMBOL(X11, XSubtractRegion, int, (Region sra, Region srb, Region dr_return), (sra, srb, dr_return))
REGISTER_SYMBOL(X11, XTextPropertyToStringList, Status, (XTextProperty* text_prop, char*** list_return, int* count_return), (text_prop, list_return, count_return))
REGISTER_SYMBOL(X11, XUnionRectWithRegion, int, (XRectangle* rectangle, Region src_region, Region dest_region_return), (rectangle, src_region, dest_region_return))
REGISTER_SYMBOL(X11, Xutf8TextListToTextProperty, int, (Display* display, char** list, int count, XICCEncodingStyle style, XTextProperty* text_prop_return), (display, list, count, style, text_prop_return))

// Functions declared in extensions/shape.h and implemented in libXext.so
REGISTER_SYMBOL_NO_RETURN(Xext, XShapeCombineMask, (Display* display, Window dest, int dest_kind, int x_off, int y_off, Pixmap src, int op), (display, dest, dest_kind, x_off, y_off, src, op))
REGISTER_SYMBOL_NO_RETURN(Xext, XShapeCombineRectangles, (Display* display, Window dest, int dest_kind, int x_off, int y_off, XRectangle* rectangles, int n_rects, int op, int ordering), (display, dest, dest_kind, x_off, y_off, rectangles, n_rects, op, ordering))
REGISTER_SYMBOL(Xext, XShapeQueryExtension, Bool, (Display* display, int* event_base, int* error_base), (display, event_base, error_base))

// Functions declared in extensions/Xdbe.h and implemented in libXext.so
REGISTER_SYMBOL(Xext, XdbeAllocateBackBufferName, XdbeBackBuffer, (Display* dpy, Window window, XdbeSwapAction swap_action), (dpy, window, swap_action))
REGISTER_SYMBOL(Xext, XdbeBeginIdiom, Status, (Display* dpy), (dpy))
REGISTER_SYMBOL(Xext, XdbeDeallocateBackBufferName, Status, (Display* dpy, XdbeBackBuffer buffer), (dpy, buffer))
REGISTER_SYMBOL(Xext, XdbeEndIdiom, Status, (Display* dpy), (dpy))
REGISTER_SYMBOL_NO_RETURN(Xext, XdbeFreeVisualInfo, (XdbeScreenVisualInfo* visual_info), (visual_info))
REGISTER_SYMBOL(Xext, XdbeGetVisualInfo, XdbeScreenVisualInfo*, (Display* dpy, Drawable* screen_specifiers, int* num_screens), (dpy, screen_specifiers, num_screens))
REGISTER_SYMBOL(Xext, XdbeQueryExtension, Status, (Display* dpy, int* major_version_return, int* minor_version_return), (dpy, major_version_return, minor_version_return))
REGISTER_SYMBOL(Xext, XdbeSwapBuffers, Status, (Display* dpy, XdbeSwapInfo* swap_info, int num_windows), (dpy, swap_info, num_windows))

// Functions declared in extensions/XShm.h and implemented in libXext.so
REGISTER_SYMBOL(Xext, XShmAttach, Bool, (Display* dpy, XShmSegmentInfo* shminfo), (dpy, shminfo))
REGISTER_SYMBOL(Xext, XShmCreateImage, XImage*, (Display* dpy, Visual* visual, unsigned int depth, int format, char* data, XShmSegmentInfo* shminfo, unsigned int width, unsigned int height), (dpy, visual, depth, format, data, shminfo, width, height))
REGISTER_SYMBOL(Xext, XShmCreatePixmap, Pixmap, (Display* dpy, Drawable d, char* data, XShmSegmentInfo* shminfo, unsigned int width, unsigned int height, unsigned int depth), (dpy, d, data, shminfo, width, height, depth))
REGISTER_SYMBOL(Xext, XShmDetach, Bool, (Display* dpy, XShmSegmentInfo* shminfo), (dpy, shminfo))
REGISTER_SYMBOL(Xext, XShmGetImage, Bool, (Display* dpy, Drawable d, XImage* image, int x, int y, unsigned long plane_mask), (dpy, d, image, x, y, plane_mask))
REGISTER_SYMBOL(Xext, XShmPixmapFormat, int, (Display* dpy), (dpy))
REGISTER_SYMBOL(Xext, XShmPutImage, Bool, (Display* dpy, Drawable d, GC gc, XImage* image, int src_x, int src_y, int dst_x, int dst_y, unsigned int src_width, unsigned int src_height, Bool send_event), (dpy, d, gc, image, src_x, src_y, dst_x, dst_y, src_width, src_height, send_event))
REGISTER_SYMBOL(Xext, XShmQueryExtension, Bool, (Display* dpy), (dpy))
REGISTER_SYMBOL(Xext, XShmQueryVersion, Bool, (Display* dpy, int* majorVersion, int* minorVersion, Bool* sharedPixmaps), (dpy, majorVersion, minorVersion, sharedPixmaps))

// Functions declared in extensions/XInput.h and implemented in libXi.so
REGISTER_SYMBOL(Xi, XFreeDeviceList, void, (XDeviceInfo* list), (list))
REGISTER_SYMBOL(Xi, XListInputDevices, XDeviceInfo*, (Display* display, int* ndevices), (display, ndevices))

// Functions declared in extensions/Xrender.h and implemented in libXrender.so
REGISTER_SYMBOL(Xrender, XRenderAddGlyphs, void, (Display* dpy, GlyphSet glyphset, _Xconst Glyph* gids, _Xconst XGlyphInfo* glyphs, int nglyphs, _Xconst char* images, int nbyte_images), (dpy, glyphset, gids, glyphs, nglyphs, images, nbyte_images))
REGISTER_SYMBOL(Xrender, XRenderChangePicture, void, (Display* dpy, Picture picture, unsigned long valuemask, _Xconst XRenderPictureAttributes* attributes), (dpy, picture, valuemask, attributes))
REGISTER_SYMBOL(Xrender, XRenderComposite, void, (Display* dpy, int op, Picture src, Picture mask, Picture dst, int src_x, int src_y, int mask_x, int mask_y, int dst_x, int dst_y, unsigned int width, unsigned int height), (dpy, op, src, mask, dst, src_x, src_y, mask_x, mask_y, dst_x, dst_y, width, height))
REGISTER_SYMBOL(Xrender, XRenderCompositeText32, void, (Display* dpy, int op, Picture src, Picture dst, _Xconst XRenderPictFormat* maskFormat, int xSrc, int ySrc, int xDst, int yDst, _Xconst XGlyphElt32* elts, int nelt), (dpy, op, src, dst, maskFormat, xSrc, ySrc, xDst, yDst, elts, nelt))
REGISTER_SYMBOL(Xrender, XRenderCreateGlyphSet, GlyphSet, (Display* dpy, _Xconst XRenderPictFormat* format), (dpy, format))
REGISTER_SYMBOL(Xrender, XRenderCreateLinearGradient, Picture, (Display* dpy, const XLinearGradient* gradient, const XFixed* stops, const XRenderColor* colors, int nstops), (dpy, gradient, stops, colors, nstops))
REGISTER_SYMBOL(Xrender, XRenderCreatePicture, Picture, (Display* dpy, Drawable drawable, _Xconst XRenderPictFormat* format, unsigned long valuemask, _Xconst XRenderPictureAttributes* attributes), (dpy, drawable, format, valuemask, attributes))
REGISTER_SYMBOL(Xrender, XRenderCreateRadialGradient, Picture, (Display* dpy, const XRadialGradient* gradient, const XFixed* stops, const XRenderColor* colors, int nstops), (dpy, gradient, stops, colors, nstops))
REGISTER_SYMBOL(Xrender, XRenderFillRectangle, void, (Display* dpy, int op, Picture dst, _Xconst XRenderColor* color, int x, int y, unsigned int width, unsigned int height), (dpy, op, dst, color, x, y, width, height))
REGISTER_SYMBOL(Xrender, XRenderFillRectangles, void, (Display* dpy, int op, Picture dst, _Xconst XRenderColor* color, _Xconst XRectangle* rectangles, int n_rects), (dpy, op, dst, color, rectangles, n_rects))
REGISTER_SYMBOL(Xrender, XRenderFindStandardFormat, XRenderPictFormat*, (Display* dpy, int format), (dpy, format))
REGISTER_SYMBOL(Xrender, XRenderFreeGlyphs, void, (Display* dpy, GlyphSet glyphset, _Xconst Glyph* gids, int nglyphs), (dpy, glyphset, gids, nglyphs))
REGISTER_SYMBOL(Xrender, XRenderFreePicture, void, (Display* dpy, Picture picture), (dpy, picture))
REGISTER_SYMBOL(Xrender, XRenderSetPictureClipRectangles, void, (Display* dpy, Picture picture, int xOrigin, int yOrigin, _Xconst XRectangle* rects, int n), (dpy, picture, xOrigin, yOrigin, rects, n))
REGISTER_SYMBOL(Xrender, XRenderSetPictureFilter, void, (Display* dpy, Picture picture, const char* filter, XFixed* params, int nparams), (dpy, picture, filter, params, nparams))
REGISTER_SYMBOL(Xrender, XRenderSetPictureTransform, void, (Display* dpy, Picture picture, XTransform* transform), (dpy, picture, transform))

// Functions declared in extensions/Xtst.h and implemented in libXtest.so
REGISTER_SYMBOL(Xtst, XTestFakeButtonEvent, int, (Display* dpy, unsigned int button, Bool is_press, unsigned long delay), (dpy, button, is_press, delay))
REGISTER_SYMBOL(Xtst, XTestFakeKeyEvent, int, (Display* dpy, unsigned int keycode, Bool is_press, unsigned long delay), (dpy, keycode, is_press, delay))
REGISTER_SYMBOL(Xtst, XTestGrabControl, int, (Display* dpy, Bool impervious), (dpy, impervious))
REGISTER_SYMBOL(Xtst, XTestQueryExtension, Bool, (Display* dpy, int* event_basep, int* error_basep, int* majorp, int* minorp), (dpy, event_basep, error_basep, majorp, minorp))


/* Dynamically load all X11 native libraries, and lookup all needed symbols */
JNIEXPORT int JNICALL initX11Symbols() {
    // Loop through all libraries
    for (int i = 0; i < lib_count; i++) {
        LibInfo* lib = &all_libs[i];
        void* lib_handle;

        lib_handle = dlopen(lib->lib_filename, RTLD_NOW | RTLD_GLOBAL);
        if (lib_handle == NULL) {
            fprintf(stderr, "Failed to load library: %s\n", dlerror());
            return -1;
        }

        // Loop through the symbols for this library and load them
        SymbolArray* symbols = lib->lib_symbols;
        int symbol_count = *lib->lib_symbol_count;

        for (int j = 0; j < symbol_count; j++) {
            SymbolInfo* symbol = &(*symbols)[j];
            const char* name = symbol->symbol_name;

            void* symbol_addr = dlsym(lib_handle, name);
            if (symbol_addr == NULL) {
                fprintf(stderr, "Failed to load symbol %s from %s: %s\n",
                        name, lib->lib_name, dlerror());
                return -1;
            }
            *symbol->symbol_ptr = symbol_addr;
        }
    }

    return 0;
}

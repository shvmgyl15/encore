/*
 * Copyright (C) 2014 Fastboot Mobile, LLC.
 *
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>.
 */
#ifndef SRC_MAIN_JNI_NATIVEPLAYER_NATIVEHUB_H_
#define SRC_MAIN_JNI_NATIVEPLAYER_NATIVEHUB_H_

#include <list>
#include <string>

class INativeSink;

class NativeHub {
 public:
    NativeHub();
    ~NativeHub();

    // Sets the active audio sink
    void setSink(INativeSink* sink);

 private:
    std::list<std::string> m_DSPChain;
    INativeSink* m_pSink;
};


#endif  // SRC_MAIN_JNI_NATIVEPLAYER_NATIVEHUB_H_

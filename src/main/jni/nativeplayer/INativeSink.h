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
#ifndef SRC_MAIN_JNI_NATIVEPLAYER_INATIVESINK_H_
#define SRC_MAIN_JNI_NATIVEPLAYER_INATIVESINK_H_

class INativeSink {
 public:
    // Enqueue buffer data if possible to the player
    // @returns The number of samples written (0 means the buffer is full)
    virtual uint32_t enqueue(const void* data, uint32_t len) = 0;
};


#endif  // SRC_MAIN_JNI_NATIVEPLAYER_INATIVESINK_H_

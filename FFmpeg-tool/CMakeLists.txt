# Author: AlanWang
# Email: alanwang4523@gmail.com
# Date: 2020-07-01

cmake_minimum_required(VERSION 3.4.1)

project (ffmpeg_tool)

set(FFMPEG_PATH ${CMAKE_CURRENT_SOURCE_DIR})
set(FFMPEG_LIB_PATH ${FFMPEG_PATH}/ffmpeg/libs/${ANDROID_ABI})

message(${ANDROID_ABI})
message(${FFMPEG_PATH})

include_directories(${FFMPEG_PATH})
include_directories(${FFMPEG_PATH}/ffmpeg/include)

file(GLOB_RECURSE SRC_FILES_FFMPEG ${FFMPEG_PATH}/*.c)

add_executable(
            ffmpeg_tool
            ${SRC_FILES_FFMPEG}
)

target_link_libraries(
                    ffmpeg_tool
                    android

                    ${FFMPEG_LIB_PATH}/libffmpeg.so

                    z m
#                    -lz -lbz2
)
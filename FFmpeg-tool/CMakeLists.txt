# Author: AlanWang
# Email: alanwang4523@gmail.com
# Date: 2020-07-01

cmake_minimum_required(VERSION 3.6)
project(ffmpeg_tool)

#set(CMAKE_CXX_STANDARD 11)

set(FFMPEG_PATH ${CMAKE_CURRENT_SOURCE_DIR})
set(FFMPEG_LIB_PATH ${FFMPEG_PATH}/ffmpeg/libs/${ANDROID_ABI})

message(${ANDROID_ABI})
message(${FFMPEG_PATH})
message(${FFMPEG_LIB_PATH})

include_directories(${FFMPEG_PATH})
include_directories(${FFMPEG_PATH}/ffmpeg/include)

# file(GLOB_RECURSE SRC_FILES_FFMPEG ${FFMPEG_PATH}/*.c)
set(SRC_FILES_FFMPEG
    ${FFMPEG_PATH}/ffmpeg.c
    ${FFMPEG_PATH}/cmdutils.c
    ${FFMPEG_PATH}/ffmpeg_filter.c
    ${FFMPEG_PATH}/ffmpeg_hw.c
    ${FFMPEG_PATH}/ffmpeg_opt.c)


find_library(log-lib log)

add_executable(ffmpeg_tool ${SRC_FILES_FFMPEG})
target_link_libraries(
                    ffmpeg_tool

                    ${log-lib}
                    android

                    ${FFMPEG_LIB_PATH}/libffmpeg.so

                    -lstdc++

                    z m
)
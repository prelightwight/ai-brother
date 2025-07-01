cmake_minimum_required(VERSION 3.10.2)

project("aibrother")

add_library(
        llama
        SHARED
        native-lib.cpp
)

find_library(
        log-lib
        log
)

target_link_libraries(
        llama
        ${log-lib}
)

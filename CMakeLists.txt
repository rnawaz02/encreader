cmake_minimum_required(VERSION 3.4.1)
project(Project)

find_package(JNI)
    if (JNI_FOUND)
        include_directories(SYSTEM ${JAVA_INCLUDE_PATH})
        include_directories(SYSTEM ${JAVA_INCLUDE_PATH2})
    endif()

add_definitions(-DANDROID )
include_directories(src/main/native/crypto)

add_library( edsxts
			 SHARED
             src/main/native/xts/edsxts.c )

target_link_libraries( edsxts )

add_library( edsaes
			 SHARED
             src/main/native/aes/edsaes.c 
             src/main/native/aes/aescrypt.c 
             src/main/native/aes/aeskey.c 
             src/main/native/aes/aestab.c )

target_link_libraries( edsaes)


add_library( edsserpent
			 SHARED
             src/main/native/serpent/edsserpent.c
             src/main/native/serpent/serpent.c )

target_link_libraries( edsserpent)

add_library( edstwofish
			 SHARED
             src/main/native/twofish/edstwofish.c
             src/main/native/twofish/twofish.c )

add_library( edsripemd160
			 SHARED
             src/main/native/ripemd160/edsripemd160.c )

target_link_libraries( edsripemd160)

add_library( edswhirlpool
			 SHARED
             src/main/native/whirlpool/edswhirlpool.c
             src/main/native/whirlpool/whirlpool.c )

target_link_libraries( edswhirlpool)


add_library( localxts
			 SHARED
             src/main/native/localxts/localxts.c )

target_compile_definitions( localxts
        					PRIVATE _FILE_OFFSET_BITS=64 )

target_link_libraries( localxts
                       edsxts)

add_library( edsctr
       SHARED
             src/main/native/ctr/edsctr.c )

target_link_libraries( edsctr)

add_library( edscbc
       SHARED
             src/main/native/cbc/edscbc.c )

target_link_libraries( edscbc)

add_library( edscfb
       SHARED
             src/main/native/cfb/edscfb.c )

target_link_libraries( edscfb)
add_library( edsgost
       SHARED
             src/main/native/gost/edsgost.c
             src/main/native/gost/gost89.c )

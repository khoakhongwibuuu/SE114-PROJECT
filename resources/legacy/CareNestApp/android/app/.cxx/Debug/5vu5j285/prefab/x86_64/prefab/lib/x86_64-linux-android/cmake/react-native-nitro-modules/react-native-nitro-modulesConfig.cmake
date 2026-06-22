if(NOT TARGET react-native-nitro-modules::NitroModules)
add_library(react-native-nitro-modules::NitroModules SHARED IMPORTED)
set_target_properties(react-native-nitro-modules::NitroModules PROPERTIES
    IMPORTED_LOCATION "D:/DoAn_MB1/CareNest/frontend/CareNestApp/node_modules/react-native-nitro-modules/android/build/intermediates/cxx/Debug/1t652y4h/obj/x86_64/libNitroModules.so"
    INTERFACE_INCLUDE_DIRECTORIES "D:/DoAn_MB1/CareNest/frontend/CareNestApp/node_modules/react-native-nitro-modules/android/build/headers/nitromodules"
    INTERFACE_LINK_LIBRARIES ""
)
endif()


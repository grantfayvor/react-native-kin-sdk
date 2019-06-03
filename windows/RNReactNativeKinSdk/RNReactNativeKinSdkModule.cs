using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace React.Native.Kin.Sdk.RNReactNativeKinSdk
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNReactNativeKinSdkModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNReactNativeKinSdkModule"/>.
        /// </summary>
        internal RNReactNativeKinSdkModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNReactNativeKinSdk";
            }
        }
    }
}

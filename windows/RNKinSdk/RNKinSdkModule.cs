using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace Kin.Sdk.RNKinSdk
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNKinSdkModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNKinSdkModule"/>.
        /// </summary>
        internal RNKinSdkModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNKinSdk";
            }
        }
    }
}


# react-native-kin-sdk

## Getting started

`$ npm install react-native-kin-sdk --save`

### Mostly automatic installation

`$ react-native link react-native-kin-sdk`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-kin-sdk` and add `RNKinSdk.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNKinSdk.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNKinSdkPackage;` to the imports at the top of the file
  - Add `new RNKinSdkPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-kin-sdk'
  	project(':react-native-kin-sdk').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-kin-sdk/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-kin-sdk')
  	```

#### Windows
[Read it! :D](https://github.com/ReactWindows/react-native)

1. In Visual Studio add the `RNKinSdk.sln` in `node_modules/react-native-kin-sdk/windows/RNKinSdk.sln` folder to their solution, reference from their app.
2. Open up your `MainPage.cs` app
  - Add `using Kin.Sdk.RNKinSdk;` to the usings at the top of the file
  - Add `new RNKinSdkPackage()` to the `List<IReactPackage>` returned by the `Packages` method


## Usage
```javascript
import RNKinSdk from 'react-native-kin-sdk';

// TODO: What to do with the module?
RNKinSdk;
```
  
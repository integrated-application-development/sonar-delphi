unit WinapiCallingConvention;

interface

uses
  Winapi.Windows;

procedure SetThreadExecutionState(ESFlags: DWORD); winapi; external 'kernel32.dll' name 'SetThreadExecutionState';

implementation

end.
unit ImplicitForwarding;

{This is a sample Delphi file.}

interface

type
  TFooPointer = ^TFoo;
  TMetaFoo = class of TFoo;

  TFoo = class(TObject)
  end;

implementation

end.
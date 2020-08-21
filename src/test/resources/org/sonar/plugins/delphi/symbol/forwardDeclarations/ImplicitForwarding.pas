unit ImplicitForwarding;

interface

type
  TFooPointer = ^TFoo;
  TMetaFoo = class of TFoo;

  TFoo = class(TObject)
  end;

implementation

end.
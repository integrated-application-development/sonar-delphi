unit DefaultProperties;

interface

const
  C_Bar = 123;

type
  TFoo = class
    property Bar: Integer default C_Bar;
  end;

implementation

end.
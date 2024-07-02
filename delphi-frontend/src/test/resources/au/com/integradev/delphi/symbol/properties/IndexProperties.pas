unit IndexProperties;

interface

const
  C_Bar = 123;

type
  TFoo = class
    property Bar: Integer index C_Bar;
  end;

implementation

end.
unit StoredProperties;

interface

const
  C_Bar = 123;

type
  TFoo = class
    property Bar: Integer stored C_Bar;
  end;

implementation

end.
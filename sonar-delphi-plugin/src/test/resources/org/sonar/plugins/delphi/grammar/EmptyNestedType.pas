unit EmptyNestedTypes;

interface

type
  SomeType = class(BaseType)

  type
    // No nested types are declared!

  private
    const
      C_MyCoolConstant = 'This is my cool constant.';
  private
    FVeryPrivateField: String;
  protected
    procedure VeryNeatProcedure;
  end;

implementation

end.
